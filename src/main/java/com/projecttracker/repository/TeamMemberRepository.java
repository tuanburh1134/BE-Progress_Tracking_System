package com.projecttracker.repository;

import com.projecttracker.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho TeamMember entity.
 */
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /** Kiểm tra user đã là thành viên (bất kỳ status) */
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    /** Kiểm tra user đã là thành viên ACCEPTED */
    boolean existsByTeamIdAndUserIdAndStatus(Long teamId, Long userId, TeamMember.TeamMemberStatus status);

    /** Lấy danh sách thành viên của nhóm theo status */
    List<TeamMember> findByTeamIdAndStatusOrderByInvitedAtDesc(Long teamId, TeamMember.TeamMemberStatus status);

    /** Lấy tất cả thành viên của nhóm */
    List<TeamMember> findByTeamIdOrderByInvitedAtDesc(Long teamId);

    /** Lấy lời mời PENDING của user (để chấp nhận/từ chối) */
    List<TeamMember> findByUserIdAndStatusOrderByInvitedAtDesc(Long userId, TeamMember.TeamMemberStatus status);

    /** Lấy record cụ thể */
    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
}
