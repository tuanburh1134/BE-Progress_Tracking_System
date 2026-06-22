package com.projecttracker.service;

import com.projecttracker.dto.response.DashboardStatsResponse;

/**
 * Service interface cho Dashboard – trả về thống kê tổng hợp của user.
 */
public interface DashboardService {

    /**
     * Lấy toàn bộ thống kê cần thiết cho Dashboard của user hiện tại.
     *
     * @param userId ID của user đang đăng nhập
     * @return DashboardStatsResponse chứa stats, dữ liệu biểu đồ
     */
    DashboardStatsResponse getStats(Long userId);
}
