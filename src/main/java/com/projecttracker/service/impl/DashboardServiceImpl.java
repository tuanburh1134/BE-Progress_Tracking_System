package com.projecttracker.service.impl;

import com.projecttracker.dto.response.DashboardStatsResponse;
import com.projecttracker.dto.response.DashboardStatsResponse.ChartItem;
import com.projecttracker.entity.Project;
import com.projecttracker.entity.Task;
import com.projecttracker.repository.ProjectRepository;
import com.projecttracker.repository.TaskRepository;
import com.projecttracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai DashboardService – tổng hợp thống kê thực từ database cho từng user.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(Long userId) {
        log.debug("Tính dashboard stats cho userId={}", userId);

        return DashboardStatsResponse.builder()
                .activeProjects(countActiveProjects(userId))
                .completedTasks(countTasksByAssignee(userId, Task.TaskStatus.DONE))
                .inProgressTasks(countTasksByAssignee(userId, Task.TaskStatus.IN_PROGRESS))
                .teamMembers(countTeamMembers(userId))
                .projectProgress(buildProjectProgress(userId))
                .taskStatusBreakdown(buildTaskStatusBreakdown(userId))
                .weeklyActivity(buildWeeklyActivity(userId))
                .build();
    }

    // -----------------------------------------------------------------------
    // StatCard helpers
    // -----------------------------------------------------------------------

    /**
     * Đếm số dự án "đang hoạt động" mà user là owner hoặc thành viên.
     * Bao gồm: PLANNING, IN_PROGRESS, ON_HOLD (tất cả trừ COMPLETED và CANCELLED).
     */
    private int countActiveProjects(Long userId) {
        long count = projectRepository.countActiveProjectsByUserId(userId);
        return (int) count;
    }

    /**
     * Đếm task theo trạng thái được giao cho user.
     */
    private long countTasksByAssignee(Long userId, Task.TaskStatus status) {
        return taskRepository.countByAssigneeIdAndStatus(userId, status);
    }

    /**
     * Đếm tổng số thành viên distinct trong tất cả dự án của user.
     */
    private long countTeamMembers(Long userId) {
        return taskRepository.countDistinctTeamMembersByUserId(userId);
    }

    // -----------------------------------------------------------------------
    // BarChart: tiến độ từng dự án
    // -----------------------------------------------------------------------

    /**
     * Tạo dữ liệu BarChart: mỗi dự án → { name: tên dự án, value: % tiến độ }.
     * Chỉ lấy tối đa 10 dự án để biểu đồ không bị quá đông.
     */
    private List<ChartItem> buildProjectProgress(Long userId) {
        return projectRepository.findActiveProjectsByUserId(userId)
                .stream()
                .limit(10)
                .map(p -> ChartItem.builder()
                        .name(p.getName())
                        .value(p.getProgress())
                        .build())
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // PieChart: phân bổ task theo trạng thái
    // -----------------------------------------------------------------------

    /**
     * Tạo dữ liệu PieChart: số task theo từng trạng thái trong tất cả dự án của user.
     */
    private List<ChartItem> buildTaskStatusBreakdown(Long userId) {
        Map<Task.TaskStatus, String> labelMap = Map.of(
                Task.TaskStatus.TODO, "Chờ xử lý",
                Task.TaskStatus.IN_PROGRESS, "Đang thực hiện",
                Task.TaskStatus.IN_REVIEW, "Đang xem xét",
                Task.TaskStatus.DONE, "Hoàn thành",
                Task.TaskStatus.BLOCKED, "Bị chặn"
        );

        List<ChartItem> items = new ArrayList<>();
        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            long count = taskRepository.countByUserProjectsAndStatus(userId, status);
            if (count > 0) {
                items.add(ChartItem.builder()
                        .name(labelMap.getOrDefault(status, status.name()))
                        .value(count)
                        .build());
            }
        }
        return items;
    }

    // -----------------------------------------------------------------------
    // LineChart: hoạt động tuần (7 ngày gần nhất)
    // -----------------------------------------------------------------------

    /**
     * Tạo dữ liệu LineChart: số task hoàn thành theo từng ngày trong 7 ngày gần nhất.
     * Các ngày không có task hoàn thành sẽ hiển thị value = 0.
     */
    private List<ChartItem> buildWeeklyActivity(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6); // 7 ngày kể cả hôm nay

        // Lấy kết quả từ DB: [completedDate, count]
        List<Object[]> rawData = taskRepository.countCompletedTasksPerDay(userId, weekAgo, today);

        // Map ngày → số lượng
        Map<LocalDate, Long> countByDate = rawData.stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> (Long) row[1]
                ));

        // Tạo danh sách 7 ngày theo thứ tự (điền 0 cho ngày không có dữ liệu)
        List<ChartItem> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String label = toDayLabel(date);
            long count = countByDate.getOrDefault(date, 0L);
            result.add(ChartItem.builder().name(label).value(count).build());
        }
        return result;
    }

    /**
     * Chuyển LocalDate sang nhãn hiển thị tiếng Việt (T2–CN, hoặc "Hôm nay").
     */
    private String toDayLabel(LocalDate date) {
        if (date.equals(LocalDate.now())) {
            return "Hôm nay";
        }
        DayOfWeek dow = date.getDayOfWeek();
        return switch (dow) {
            case MONDAY    -> "T2";
            case TUESDAY   -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY  -> "T5";
            case FRIDAY    -> "T6";
            case SATURDAY  -> "T7";
            case SUNDAY    -> "CN";
        };
    }
}
