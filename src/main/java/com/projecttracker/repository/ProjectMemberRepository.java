package com.projecttracker.repository;

import com.projecttracker.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ProjectMember entity.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * Kiểm tra user đã là thành viên của dự án chưa.
     */
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * Lấy tất cả thành viên của một dự án.
     */
    List<ProjectMember> findByProjectId(Long projectId);

    /**
     * Tìm record thành viên cụ thể.
     */
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * Xóa thành viên khỏi dự án.
     */
    void deleteByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * Đếm số thành viên của dự án.
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);
}
