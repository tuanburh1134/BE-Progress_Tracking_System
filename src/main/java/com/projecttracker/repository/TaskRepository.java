package com.projecttracker.repository;

import com.projecttracker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository cho Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Lấy tất cả task của một dự án, sắp xếp theo status và displayOrder.
     * Dùng để render Kanban board.
     *
     * @param projectId ID của dự án
     * @return Danh sách task đã sắp xếp
     */
    List<Task> findByProjectIdOrderByStatusAscDisplayOrderAsc(Long projectId);

    /**
     * Lấy tất cả task được phân công cho một user trong một dự án.
     *
     * @param projectId  ID dự án
     * @param assigneeId ID người được phân công
     * @return Danh sách task
     */
    List<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId);

    /**
     * Đếm task theo trạng thái trong một dự án.
     * Dùng để tính tiến độ dự án.
     *
     * @param projectId ID dự án
     * @param status    Trạng thái cần đếm
     * @return Số lượng task
     */
    long countByProjectIdAndStatus(Long projectId, Task.TaskStatus status);

    /**
     * Lấy tổng số task của một dự án.
     *
     * @param projectId ID dự án
     * @return Tổng số task
     */
    long countByProjectId(Long projectId);

    /**
     * Tìm các task có deadline gần đến hạn (dùng cho cảnh báo AI).
     *
     * @param startDate Ngày bắt đầu khoảng thời gian
     * @param endDate   Ngày kết thúc khoảng thời gian
     * @return Danh sách task sắp đến hạn
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.deadline BETWEEN :startDate AND :endDate
            AND t.status NOT IN ('DONE')
            ORDER BY t.deadline ASC
            """)
    List<Task> findTasksWithUpcomingDeadline(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu task để AI phân tích (estimated vs actual hours).
     * Chỉ lấy task đã hoàn thành có đủ dữ liệu.
     *
     * @param projectId ID dự án
     * @return Danh sách task đã DONE có đủ dữ liệu time tracking
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
            AND t.status = 'DONE'
            AND t.estimatedHours IS NOT NULL
            AND t.actualHours IS NOT NULL
            """)
    List<Task> findCompletedTasksWithTimeData(@Param("projectId") Long projectId);
}
