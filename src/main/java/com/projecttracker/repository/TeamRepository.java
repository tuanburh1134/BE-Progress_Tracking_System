package com.projecttracker.repository;

import com.projecttracker.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /** Nhóm mà user là owner hoặc thành viên */
    @Query("""
        SELECT DISTINCT t FROM Team t
        LEFT JOIN t.members tm
        WHERE t.owner.id = :userId OR tm.user.id = :userId
        ORDER BY t.createdAt DESC
    """)
    Page<Team> findTeamsByUserId(@Param("userId") Long userId, Pageable pageable);
}
