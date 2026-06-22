package com.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO tổng hợp toàn bộ thống kê cần thiết cho Dashboard.
 *
 * <p>Trả về trong một request duy nhất để tránh nhiều round-trip.</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    /** Số dự án đang IN_PROGRESS mà user là owner hoặc thành viên */
    private int activeProjects;

    /** Số task đã DONE được giao cho user */
    private long completedTasks;

    /** Số task đang IN_PROGRESS được giao cho user */
    private long inProgressTasks;

    /** Tổng số thành viên distinct trong tất cả dự án của user */
    private long teamMembers;

    /** Tiến độ từng dự án dùng cho BarChart */
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
