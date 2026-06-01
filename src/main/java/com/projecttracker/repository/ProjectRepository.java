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
            SELECT DISTINCT p FROM Project p
            LEFT JOIN p.members pm
            WHERE p.owner.id = :userId OR pm.user.id = :userId
            ORDER BY p.createdAt DESC
            """)
    Page<Project> findProjectsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Lấy danh sách dự án đang IN_PROGRESS của một user.
     * Dùng cho dashboard tổng quan.
     *
     * @param userId ID của user
     * @return Danh sách dự án đang thực hiện
     */
    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN p.members pm
            WHERE (p.owner.id = :userId OR pm.user.id = :userId)
            AND p.status = 'IN_PROGRESS'
            """)
    List<Project> findActiveProjectsByUserId(@Param("userId") Long userId);

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
            AND p.status = :status
            """)
    long countByUserIdAndStatus(@Param("userId") Long userId,
                                @Param("status") Project.ProjectStatus status);
}
