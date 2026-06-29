package com.projecttracker.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.projecttracker.entity.BuildReport;
import com.projecttracker.repository.BuildReportRepository;
import com.projecttracker.service.CiCdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CiCdServiceImpl implements CiCdService {

    private final BuildReportRepository buildReportRepository;
    private final ObjectMapper objectMapper;

    @Value("${project.root-path}")
    private String projectRootPath;

    @Override
    @Transactional(readOnly = true)
    public List<BuildReport> getBuildReports() {
        return buildReportRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Async
    @Transactional
    public void processWebhook(Map<String, Object> payload) {
        log.info("Bắt đầu xử lý GitHub Webhook...");

        // Parse commit details from payload
        String commitHash = "unknown";
        String commitMessage = "unknown";
        String author = "unknown";
        String branch = "unknown";

        try {
            if (payload.containsKey("ref")) {
                String ref = (String) payload.get("ref");
                branch = ref.replace("refs/heads/", "");
            }

            if (payload.containsKey("head_commit")) {
                Map<String, Object> headCommit = (Map<String, Object>) payload.get("head_commit");
                commitHash = (String) headCommit.get("id");
                commitMessage = (String) headCommit.get("message");
                
                Map<String, Object> authorMap = (Map<String, Object>) headCommit.get("author");
                author = (String) authorMap.get("name");
            }
        } catch (Exception e) {
            log.error("Lỗi khi parse payload webhook: {}", e.getMessage());
        }

        // Tạo bản ghi BuildReport ban đầu
        BuildReport report = BuildReport.builder()
                .commitHash(commitHash)
                .commitMessage(commitMessage)
                .author(author)
                .branch(branch)
                .status(BuildReport.BuildStatus.RUNNING)
                .log("Khởi động tiến trình kiểm thử tự động...\n")
                .createdAt(LocalDateTime.now())
                .build();

        report = buildReportRepository.save(report);

        StringBuilder buildLog = new StringBuilder();
        buildLog.append("=== AI CI/CD PIPELINE START ===\n");
        buildLog.append("Thời gian bắt đầu: ").append(LocalDateTime.now()).append("\n");
        buildLog.append("Branch: ").append(branch).append("\n");
        buildLog.append("Commit Hash: ").append(commitHash).append("\n");
        buildLog.append("Commit Message: ").append(commitMessage).append("\n");
        buildLog.append("Author: ").append(author).append("\n\n");

        File projectDir = new File(projectRootPath);
        File backendDir = new File(projectRootPath, "backend");

        try {
            // Bước 1: Git Pull
            buildLog.append("[Step 1/4] Đang kéo code mới nhất từ GitHub...\n");
            String gitPullLog = runCommand("git pull origin " + branch, projectDir);
            buildLog.append(gitPullLog).append("\n");

            // Bước 2: Git Diff để xem các file thay đổi
            buildLog.append("[Step 2/4] Phân tích các file thay đổi...\n");
            // So sánh commit hiện tại và commit trước đó
            String gitDiffLog = runCommand("git diff --name-only HEAD~1 HEAD", projectDir);
            buildLog.append("Các file thay đổi:\n").append(gitDiffLog).append("\n");

            List<String> modifiedJavaFiles = new ArrayList<>();
            for (String file : gitDiffLog.split("\n")) {
                String trimmed = file.trim();
                // Chỉ lấy file Java thuộc module backend
                if (trimmed.startsWith("backend/src/main/java/") && trimmed.endsWith(".java")) {
                    modifiedJavaFiles.add(trimmed);
                }
            }

            StringBuilder aiSuggestions = new StringBuilder();

            if (modifiedJavaFiles.isEmpty()) {
                buildLog.append("Không phát hiện thay đổi nào ở code Java backend cần viết test.\n\n");
            } else {
                buildLog.append("Phát hiện ").append(modifiedJavaFiles.size()).append(" file Java thay đổi. Đang gửi yêu cầu tới AI để sinh unit test...\n");
                
                // Bước 3: AI sinh test case cho từng file
                buildLog.append("[Step 3/4] AI đang sinh unit test...\n");
                for (String relPath : modifiedJavaFiles) {
                    File fileToTest = new File(projectDir, relPath);
                    if (fileToTest.exists()) {
                        buildLog.append("-> Đang xử lý file: ").append(relPath).append("\n");
                        String fileContent = readFileContent(fileToTest);
                        String generatedTestCode = generateUnitTestFromAI(fileContent, fileToTest.getName());

                        if (generatedTestCode != null && !generatedTestCode.trim().isEmpty()) {
                            // Tạo file test local
                            saveGeneratedTestFile(relPath, generatedTestCode);
                            buildLog.append("   [Thành công] Đã lưu file test cho ").append(fileToTest.getName()).append("\n");
                            aiSuggestions.append("Đã sinh Unit Test cho: ").append(relPath).append("\n");
                        } else {
                            buildLog.append("   [Bỏ qua] AI không phản hồi hoặc không sinh được code kiểm thử.\n");
                        }
                    }
                }
                buildLog.append("\n");
            }

            // Bước 4: Chạy mvn test
            buildLog.append("[Step 4/4] Đang chạy các ca kiểm thử tự động (mvn test)...\n");
            String mavenLog = runCommand("mvn test", backendDir);
            buildLog.append(mavenLog).append("\n");

            // Phân tích kết quả test qua log của Maven
            boolean testSuccess = mavenLog.contains("BUILD SUCCESS") && !mavenLog.contains("BUILD FAILURE");
            
            report.setStatus(testSuccess ? BuildReport.BuildStatus.SUCCESS : BuildReport.BuildStatus.FAILED);
            report.setLog(buildLog.toString());
            report.setAiSuggestions(aiSuggestions.length() > 0 ? aiSuggestions.toString() : "Không có gợi ý AI nào được áp dụng.");
            report.setUpdatedAt(LocalDateTime.now());

            buildReportRepository.save(report);
            log.info("Pipeline hoàn tất với trạng thái: {}", report.getStatus());

        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng trong CI/CD pipeline: {}", e.getMessage(), e);
            buildLog.append("\nLỖI HỆ THỐNG: ").append(e.getMessage()).append("\n");
            
            report.setStatus(BuildReport.BuildStatus.FAILED);
            report.setLog(buildLog.toString());
            report.setUpdatedAt(LocalDateTime.now());
            buildReportRepository.save(report);
        }
    }

    /* ================= HELPER METHODS ================= */

    private String runCommand(String command, File workingDir) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.directory(workingDir);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            process.waitFor();
        } catch (Exception e) {
            output.append("Error executing command: ").append(e.getMessage()).append("\n");
        }
        return output.toString();
    }

    private String readFileContent(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    private String generateUnitTestFromAI(String javaCode, String fileName) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            // Tạo request body
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", "gemini-2.5-flash");
            
            var messagesArray = objectMapper.createArrayNode();
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            
            String prompt = "You are an expert Java developer. Write a complete JUnit 5 unit test class using Mockito (if needed) for the following Java class. " +
                    "Return ONLY the clean, raw Java code. Do not wrap the code in ```java code blocks or markdown, just return the code beginning with the package declaration. " +
                    "Ensure the test class has correct imports, package declarations, and is named " + fileName.replace(".java", "AiTest") + ". " +
                    "Here is the Java class to test:\n\n" + javaCode;
            
            userMessage.put("content", prompt);
            messagesArray.add(userMessage);
            rootNode.set("messages", messagesArray);

            String requestBody = objectMapper.writeValueAsString(rootNode);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3001/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer freellmapi-de7ad8f28215f83317c514c021a6f6ea2b04bef1c52200f3")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                String responseText = responseJson.path("choices").path(0).path("message").path("content").asText();
                return cleanJavaCode(responseText);
            } else {
                log.error("AI service trả về lỗi HTTP {}: {}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Lỗi khi kết nối với AI để sinh unit test: {}", e.getMessage());
        }
        return null;
    }

    private String cleanJavaCode(String rawCode) {
        if (rawCode == null) return "";
        // Nếu AI bọc trong backticks markdown, ta sẽ bóc ra
        String cleaned = rawCode;
        if (cleaned.contains("```java")) {
            cleaned = cleaned.substring(cleaned.indexOf("```java") + 7);
            if (cleaned.contains("```")) {
                cleaned = cleaned.substring(0, cleaned.indexOf("```"));
            }
        } else if (cleaned.contains("```")) {
            cleaned = cleaned.substring(cleaned.indexOf("```") + 3);
            if (cleaned.contains("```")) {
                cleaned = cleaned.substring(0, cleaned.indexOf("```"));
            }
        }
        return cleaned.trim();
    }

    private void saveGeneratedTestFile(String relPath, String testCode) {
        try {
            // Ví dụ: relPath = backend/src/main/java/com/projecttracker/controller/UserController.java
            // Ta cần chuyển thành: backend/src/test/java/com/projecttracker/controller/UserControllerAiTest.java
            String testRelPath = relPath
                    .replace("src/main/java", "src/test/java")
                    .replace(".java", "AiTest.java");

            Path testFilePath = Paths.get(projectRootPath, testRelPath);
            Files.createDirectories(testFilePath.getParent());
            Files.writeString(testFilePath, testCode);
            log.info("Lưu thành công file test tại: {}", testFilePath.toAbsolutePath());
        } catch (Exception e) {
            log.error("Lỗi khi lưu file test: {}", e.getMessage());
        }
    }
}
