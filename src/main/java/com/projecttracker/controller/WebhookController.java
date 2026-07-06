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
/**
 * Controller quản lý các sự kiện CI/CD và Webhook.
 * Đóng vai trò là điểm tiếp nhận dữ liệu từ GitHub và cung cấp dữ liệu báo cáo cho Frontend.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor// Tự động inject các dependency thông qua constructor (Lombok)
@Slf4j// Hỗ trợ ghi log hệ thống
@Tag(name = "CI/CD & Webhooks", description = "Endpoints phục vụ tích hợp CI/CD tự động và webhook GitHub")
public class WebhookController {

    private final CiCdService ciCdService;

    /**
     * Endpoint tiếp nhận thông tin sự kiện push từ GitHub Webhook.
     * 
     * @param eventType Loại sự kiện (Header 'X-GitHub-Event' từ GitHub)
     * @param payload   Dữ liệu JSON chi tiết về sự kiện push
     * @return ResponseEntity thông báo trạng thái tiếp nhận
     */
    @PostMapping("/webhooks/github")
    @Operation(summary = "GitHub Webhook Receiver", description = "Đón tiếp các payload push event tự động từ GitHub")
    public ResponseEntity<Map<String, String>> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestBody Map<String, Object> payload) {

        log.info("Nhận sự kiện Webhook từ GitHub: X-GitHub-Event = {}", eventType);

        // Chỉ xử lý sự kiện push
        if ("push".equalsIgnoreCase(eventType)) {
            // Chạy bất đồng bộ (async) để giải phóng request sớm, 
            // tránh việc GitHub ngắt kết nối do vượt quá thời gian phản hồi (timeout)
            ciCdService.processWebhook(payload);
            return ResponseEntity.accepted().body(Map.of("message", "Đã nhận sự kiện push, đang chạy tiến trình CI/CD..."));
        }
        // Bỏ qua các loại sự kiện khác (ví dụ: pull_request, issue,...)
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
    /**
     * Lấy danh sách lịch sử build theo ID của dự án cụ thể.
     * 
     * @param projectId ID của dự án cần truy vấn
     */
    @GetMapping("/cicd/reports/project/{projectId}")
    @Operation(summary = "Lấy lịch sử báo cáo CI/CD của một dự án")
    public ResponseEntity<ApiResponse<List<BuildReport>>> getBuildReportsByProject(@PathVariable Long projectId) {
        List<BuildReport> reports = ciCdService.getBuildReportsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(reports, "Lấy lịch sử CI/CD của dự án thành công"));
    }
}
