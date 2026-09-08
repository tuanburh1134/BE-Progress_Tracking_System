package com.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO tổng hợp toàn bộ thống kê thực cho Dashboard.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    /** Số dự án đang hoạt động mà user tham gia */
    private int activeProjects;

    /** Tổng số dự án đã làm từ trước đến giờ (bao gồm cả đã hoàn thành hoặc xóa) */
    private long totalProjects;

    /** Tiến độ chung của dự án (% hoàn thành tổng thể) */
    private int overallProgress;

    /** Thông số Build */
    private long totalBuilds;
    private long successfulBuilds;
    private long failedBuilds;
    private int buildSuccessRate;

    /** Số task đã DONE được giao cho user */
    private long completedTasks;

    /** Số task đang IN_PROGRESS được giao cho user */
    private long inProgressTasks;

    /** Tổng số thành viên distinct trong tất cả dự án của user */
    private long teamMembers;

    /** Tiến độ từng dự án dùng cho BarChart (% tiến độ từng dự án) */
    private List<ChartItem> projectProgress;

    /** Phân bổ task theo trạng thái dùng cho PieChart */
    private List<ChartItem> taskStatusBreakdown;

    /** Số task hoàn thành theo từng ngày trong 7 ngày gần nhất dùng cho LineChart */
    private List<ChartItem> weeklyActivity;

    // -----------------------------------------------------------------------
    // Inner record cho dữ liệu biểu đồ
    // -----------------------------------------------------------------------

    /**
     * Điểm dữ liệu cho biểu đồ: tên nhãn và giá trị số.
     */
    @Getter
    @Builder
    public static class ChartItem {
        /** Nhãn hiển thị (tên dự án, tên trạng thái, thứ trong tuần…) */
        private String name;

        /** Giá trị số tương ứng */
        private long value;
    }
}
