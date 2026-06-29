package com.projecttracker.repository;

import com.projecttracker.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Lấy tất cả dự án mà user là owner hoặc thành viên.
     * Dùng JOIN để tránh N+1 query.
     *
     * @param userId   ID của user
     * @param pageable Phân trang
     * @return Page<Project> theo phân trang
     */
        @Query("""
        SELECT DISTINCT p
        FROM Project p
        LEFT JOIN p.members pm
        WHERE (p.owner.id = :userId OR pm.user.id = :userId)
        AND p.deleted = false
        ORDER BY p.createdAt DESC
        """)
        Page<Project> findProjectsByUserId(
                @Param("userId") Long userId,
                Pageable pageable);
    /**
     * Lấy danh sách dự án "đang hoạt động" của một user.
     * "Đang hoạt động" = chưa hoàn thành hay hủy (PLANNING, IN_PROGRESS, ON_HOLD).
     * Dùng cho BarChart tiến độ dashboard.
     *
     * @param userId ID của user
     * @return Danh sách dự án đang hoạt động
     */
    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN p.members pm
            WHERE (p.owner.id = :userId OR pm.user.id = :userId)
            AND p.deleted = false
            AND p.status NOT IN ('COMPLETED', 'CANCELLED')
            ORDER BY p.createdAt DESC
            """)
    List<Project> findActiveProjectsByUserId(@Param("userId") Long userId);

    /**
     * Đếm số dự án "đang hoạt động" của một user.
     * "Đang hoạt động" = chưa hoàn thành hay hủy (PLANNING, IN_PROGRESS, ON_HOLD).
     * Dùng cho StatCard "Đự Án Đang Hoạt Động".
     *
     * @param userId ID của user
     * @return Số dự án đang hoạt động
     */
    @Query("""
            SELECT COUNT(DISTINCT p) FROM Project p
            LEFT JOIN p.members pm
            WHERE (p.owner.id = :userId OR pm.user.id = :userId)
            AND p.deleted = false
            AND p.status NOT IN ('COMPLETED', 'CANCELLED')
            """)
    long countActiveProjectsByUserId(@Param("userId") Long userId);

    /**
     * Đếm tổng số dự án theo trạng thái của một user.
     * Dùng cho dashboard statistics.
     *
     * @param userId ID của user
     * @param status Trạng thái cần đếm
     * @return Số lượng dự án
     */
    @Query("""
            SELECT COUNT(DISTINCT p) FROM Project p
            LEFT JOIN p.members pm
            WHERE (p.owner.id = :userId OR pm.user.id = :userId)
            AND p.deleted = false
            AND p.status = :status
            """)
    long countByUserIdAndStatus(@Param("userId") Long userId,
                                @Param("status") Project.ProjectStatus status);
        

        /**
     * query lấy Thùng rác.
     * Dùng khôi phục dự án đã xoá tạm.
     *
     * @param userId ID của user
     * @param pageable Phân trang
     * @return Page<Project> theo phân trang
     */
    @Query("""
        SELECT DISTINCT p
        FROM Project p
        LEFT JOIN p.members pm
        WHERE (p.owner.id = :userId OR pm.user.id = :userId)
        AND p.deleted = true
        ORDER BY p.deletedAt DESC
        """)
        Page<Project> findDeletedProjectsByUserId(
        @Param("userId") Long userId,
        Pageable pageable);        

    List<Project> findByGithubLinkIsNotNull();
}
