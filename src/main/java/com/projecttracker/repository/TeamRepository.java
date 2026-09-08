package com.projecttracker.repository;

import com.projecttracker.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository cho Team entity.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Lấy danh sách nhóm mà user là owner.
     */
    List<Team> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    /**
     * Lấy tất cả nhóm mà user tham gia (ACCEPTED) hoặc là owner.
     */
    @Query("""
            SELECT DISTINCT t FROM Team t
            LEFT JOIN t.members m
            WHERE t.owner.id = :userId
               OR (m.user.id = :userId AND m.status = 'ACCEPTED')
            ORDER BY t.createdAt DESC
            """)
    List<Team> findAllTeamsForUser(@Param("userId") Long userId);
}
