package com.projecttracker.service.impl;

import com.projecttracker.dto.response.TeamMemberResponse;
import com.projecttracker.dto.response.TeamResponse;
import com.projecttracker.entity.*;
import com.projecttracker.exception.BusinessException;
import com.projecttracker.exception.ResourceNotFoundException;
import com.projecttracker.repository.*;
import com.projecttracker.service.NotificationService;
import com.projecttracker.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Triển khai TeamService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository       teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository       userRepository;
    private final NotificationService  notificationService;

    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public TeamResponse createTeam(String name, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Team team = Team.builder()
                .name(name.trim())
                .owner(owner)
                .build();
        team = teamRepository.save(team);

        // Lưu owner vào danh sách team_members với trạng thái ACCEPTED
        TeamMember ownerMember = TeamMember.builder()
                .team(team)
                .user(owner)
                .inviter(owner)
                .status(TeamMember.TeamMemberStatus.ACCEPTED)
                .respondedAt(LocalDateTime.now())
                .build();
        teamMemberRepository.save(ownerMember);

        log.info("Tạo nhóm '{}' bởi userId={}", name, ownerId);
        return TeamResponse.summary(team, 1);
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams(Long userId) {
        return teamRepository.findAllTeamsForUser(userId).stream()
                .map(team -> {
                    List<TeamMember> members = teamMemberRepository.findByTeamIdOrderByInvitedAtDesc(team.getId());
                    long count = members.stream()
                            .filter(m -> m.getStatus() == TeamMember.TeamMemberStatus.ACCEPTED)
                            .count();
                    return TeamResponse.summary(team, (int) count);
                })
                .toList();
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long teamId, Long requesterId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        return teamMemberRepository.findByTeamIdOrderByInvitedAtDesc(teamId).stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public TeamMemberResponse inviteMember(Long teamId, String email, Long inviterId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        // Chỉ owner mới được mời
        if (!team.getOwner().getId().equals(inviterId)) {
            throw new BusinessException("Chỉ owner mới có thể mời thành viên vào nhóm");
        }

        String targetEmail = email != null ? email.trim() : "";
        User invitee = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với email: " + targetEmail));

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", inviterId));

        // Không mời chính mình
        if (invitee.getId().equals(inviterId)) {
            throw new BusinessException("Bạn không thể mời chính mình vào nhóm");
        }

        // Đã là thành viên hoặc đang pending
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, invitee.getId())) {
            throw new BusinessException("Người dùng này đã được mời hoặc đã là thành viên của nhóm");
        }

        // Tạo TeamMember PENDING
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(invitee)
                .inviter(inviter)
                .status(TeamMember.TeamMemberStatus.PENDING)
                .build();
        teamMember = teamMemberRepository.save(teamMember);

        // Gửi thông báo cho invitee
        String inviterName = inviter.getFullName() != null && !inviter.getFullName().isBlank()
                ? inviter.getFullName() : inviter.getUsername();

        String message = String.format("'%s' đã mời bạn tham gia nhóm '%s'",
                inviterName, team.getName());
        notificationService.createNotification(
                invitee,
                Notification.NotificationType.TEAM_INVITATION_RECEIVED,
                message,
                teamMember.getId()
        );

        log.info("Đã gửi lời mời nhóm từ userId={} đến email={} vào team id={}", inviterId, targetEmail, teamId);
        return TeamMemberResponse.from(teamMember);
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getPendingInvitations(Long userId) {
        return teamMemberRepository
                .findByUserIdAndStatusOrderByInvitedAtDesc(userId, TeamMember.TeamMemberStatus.PENDING)
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void acceptInvitation(Long teamMemberId, Long userId) {
        TeamMember tm = getTeamMemberForUser(teamMemberId, userId);

        tm.setStatus(TeamMember.TeamMemberStatus.ACCEPTED);
        tm.setRespondedAt(LocalDateTime.now());
        teamMemberRepository.save(tm);

        // Thông báo cho owner
        String message = String.format("'%s' đã chấp nhận lời mời tham gia nhóm '%s'",
                tm.getUser().getFullName(), tm.getTeam().getName());
        notificationService.createNotification(
                tm.getInviter(),
                Notification.NotificationType.TEAM_INVITATION_ACCEPTED,
                message,
                teamMemberId
        );

        log.info("userId={} đã chấp nhận lời mời nhóm teamMemberId={}", userId, teamMemberId);
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void declineInvitation(Long teamMemberId, Long userId) {
        TeamMember tm = getTeamMemberForUser(teamMemberId, userId);

        tm.setStatus(TeamMember.TeamMemberStatus.DECLINED);
        tm.setRespondedAt(LocalDateTime.now());
        teamMemberRepository.save(tm);

        // Thông báo cho owner
        String message = String.format("'%s' đã từ chối lời mời tham gia nhóm '%s'",
                tm.getUser().getFullName(), tm.getTeam().getName());
        notificationService.createNotification(
                tm.getInviter(),
                Notification.NotificationType.TEAM_INVITATION_DECLINED,
                message,
                teamMemberId
        );

        log.info("userId={} đã từ chối lời mời nhóm teamMemberId={}", userId, teamMemberId);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private TeamMember getTeamMemberForUser(Long teamMemberId, Long userId) {
        TeamMember tm = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", "id", teamMemberId));

        if (!tm.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền phản hồi lời mời này");
        }

        if (tm.getStatus() != TeamMember.TeamMemberStatus.PENDING) {
            throw new BusinessException("Lời mời này đã được xử lý rồi");
        }

        return tm;
    }
}
