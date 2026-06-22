package com.projecttracker.controller;

import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.DashboardStatsResponse;
import com.projecttracker.security.UserPrincipal;
import com.projecttracker.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller cung cấp thống kê tổng hợp cho Dashboard.
 *
 * <p>Base URL: /api/dashboard</p>
 * <p>Tất cả endpoint yêu cầu JWT token.</p>
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Thống kê tổng hợp cho Dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Lấy toàn bộ thống kê Dashboard của user đang đăng nhập.
     *
     * <p>GET /api/dashboard/stats</p>
     *
     * <p>Trả về trong một request: stat cards, dữ liệu cho BarChart,
     * PieChart và LineChart. Frontend không cần thực hiện nhiều request.</p>
     */
    @GetMapping("/stats")
    @Operation(
            summary = "Lấy thống kê Dashboard",
            description = "Trả về toàn bộ số liệu thật theo tài khoản đang đăng nhập: " +
                          "số dự án, task, thành viên và dữ liệu biểu đồ"
    )
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        DashboardStatsResponse stats = dashboardService.getStats(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê Dashboard thành công"));
    }
}
