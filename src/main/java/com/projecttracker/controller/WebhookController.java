package com.projecttracker.controller;

import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.entity.BuildReport;
import com.projecttracker.service.CiCdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CI/CD & Webhooks", description = "Endpoints phục vụ tích hợp CI/CD tự động và webhook GitHub")
public class WebhookController {

    private final CiCdService ciCdService;

    /**
     * Endpoint tiếp nhận thông tin sự kiện push từ GitHub Webhook.
     * Cổng này công khai hoàn toàn để GitHub có thể truy cập được thông qua ngrok.
     */
    @PostMapping("/webhooks/github")
    @Operation(summary = "GitHub Webhook Receiver", description = "Đón tiếp các payload push event tự động từ GitHub")
    public ResponseEntity<Map<String, String>> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestBody Map<String, Object> payload) {

        log.info("Nhận sự kiện Webhook từ GitHub: X-GitHub-Event = {}", eventType);

        // Chỉ xử lý sự kiện push
        if ("push".equalsIgnoreCase(eventType)) {
            // Chạy bất đồng bộ tiến trình để tránh GitHub timeout (GitHub yêu cầu phản hồi < 10s)
            ciCdService.processWebhook(payload);
            return ResponseEntity.accepted().body(Map.of("message", "Đã nhận sự kiện push, đang chạy tiến trình CI/CD..."));
        }

        return ResponseEntity.ok(Map.of("message", "Sự kiện được bỏ qua."));
    }

    /**
     * API trả về lịch sử chạy thử nghiệm tự động.
     * Endpoint này yêu cầu Bearer Token khi gọi từ web.
     */
    @GetMapping("/cicd/reports")
    @Operation(summary = "Lấy lịch sử báo cáo CI/CD")
    public ResponseEntity<ApiResponse<List<BuildReport>>> getBuildReports() {
        List<BuildReport> reports = ciCdService.getBuildReports();
        return ResponseEntity.ok(ApiResponse.success(reports, "Lấy lịch sử CI/CD thành công"));
    }
}
