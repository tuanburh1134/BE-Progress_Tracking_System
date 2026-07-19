package com.projecttracker.repository;

import com.projecttracker.entity.Project;
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

    /**
     * Đếm task theo trạng thái được giao cho một user cụ thể.
     * Dùng cho dashboard: Công Việc Hoàn Thành / Đang Thực Hiện.
     *
     * @param assigneeId ID người được phân công
     * @param status     Trạng thái cần đếm
     * @return Số lượng task
     */
    long countByAssigneeIdAndStatus(Long assigneeId, Task.TaskStatus status);

        /**
         * Đếm số task theo trạng thái trên toàn hệ thống.
         * Thêm để hỗ trợ các thống kê admin (tổng completed / in-progress).
         */
        long countByStatus(Task.TaskStatus status);

    /**
     * Đếm task theo trạng thái trong tất cả dự án mà user tham gia (owner hoặc member).
     * Dùng cho PieChart phân bổ trạng thái công việc.
     *
     * @param userId ID user
     * @param status Trạng thái cần đếm
     * @return Số lượng task
     */
    @Query("""
            SELECT COUNT(t) FROM Task t
            JOIN t.project p
            LEFT JOIN p.members pm
            WHERE (p.owner.id = :userId OR pm.user.id = :userId)
            AND t.status = :status
            """)
    long countByUserProjectsAndStatus(@Param("userId") Long userId,
                                      @Param("status") Task.TaskStatus status);

    /**
     * Đếm số task hoàn thành theo ngày trong khoảng thời gian (dùng cho LineChart weekly).
     * Chỉ tính task trong các dự án mà user là owner hoặc member.
     *
     * @param userId    ID user
     * @param startDate Ngày bắt đầu khoảng (inclusive)
     * @param endDate   Ngày kết thúc khoảng (inclusive)
     * @return Danh sách [completedDate, count] theo từng ngày
     */
    @Query("""
            SELECT t.completedDate, COUNT(t) FROM Task t
            JOIN t.project p
            LEFT JOIN p.members pm
            WHERE (p.owner.id = :userId OR pm.user.id = :userId)
            AND t.status = 'DONE'
            AND t.completedDate BETWEEN :startDate AND :endDate
            GROUP BY t.completedDate
            ORDER BY t.completedDate ASC
            """)
    List<Object[]> countCompletedTasksPerDay(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * Đếm tổng số thành viên distinct trong tất cả dự án mà user tham gia.
     * Dùng cho StatCard "Thành Viên Nhóm".
     *
     * @param userId ID user
     * @return Số thành viên distinct (không tính bản thân user)
     */
    @Query("""
            SELECT COUNT(DISTINCT pm2.user.id) FROM ProjectMember pm2
            WHERE pm2.project.id IN (
                SELECT DISTINCT p.id FROM Project p
                LEFT JOIN p.members pm
                WHERE p.owner.id = :userId OR pm.user.id = :userId
            )
            """)
    long countDistinctTeamMembersByUserId(@Param("userId") Long userId);

        /**
         * Đếm số task quá hạn (deadline trước ngày được truyền vào), không tính task đã DONE.
         * Default method sử dụng method derived để tránh viết JPQL với enum trực tiếp.
         */
        long countByDeadlineBeforeAndStatusNot(LocalDate date, Task.TaskStatus status);

        default long countOverdueTasks(LocalDate date) {
                return countByDeadlineBeforeAndStatusNot(date, Task.TaskStatus.DONE);
        }
}
