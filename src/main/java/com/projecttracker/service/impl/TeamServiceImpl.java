package com.projecttracker.service.impl;

import com.projecttracker.dto.request.TeamRequest;
import com.projecttracker.dto.response.TeamResponse;
import com.projecttracker.dto.response.UserSearchResponse;
import com.projecttracker.entity.Team;
import com.projecttracker.entity.TeamMember;
import com.projecttracker.entity.User;
import com.projecttracker.exception.BusinessException;
import com.projecttracker.exception.ResourceNotFoundException;
import com.projecttracker.repository.TeamMemberRepository;
import com.projecttracker.repository.TeamRepository;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.service.NotificationService;
import com.projecttracker.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        Team saved = teamRepository.save(team);
        log.info("Tạo nhóm '{}' bởi userId={}", saved.getName(), ownerId);
        return TeamResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponse> getMyTeams(Long userId, Pageable pageable) {
        return teamRepository.findTeamsByUserId(userId, pageable)
                .map(TeamResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long teamId, Long userId) {
        Team team = findTeamOrThrow(teamId);
        validateAccess(team, userId);
        return TeamResponse.from(team);
    }

    @Override
    @Transactional
    public UserSearchResponse addMember(Long teamId, String email, Long ownerId) {
        Team team = findTeamOrThrow(teamId);
        validateOwner(team, ownerId);

        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với email: " + email));

        if (invitee.getId().equals(ownerId)) {
            throw new BusinessException("Bạn không thể mời chính mình vào nhóm");
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, invitee.getId())) {
            throw new BusinessException("Người dùng này đã là thành viên của nhóm");
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(invitee)
                .build();
        teamMemberRepository.save(member);

        // Gửi thông báo cho người được mời
        notificationService.createNotification(
                invitee.getId(),
                "ADDED_TO_TEAM",
                "Bạn đã được thêm vào nhóm \"" + team.getName() + "\"",
                teamId
        );

        log.info("Thêm userId={} vào nhóm id={}", invitee.getId(), teamId);
        return UserSearchResponse.from(invitee);
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long memberId, Long ownerId) {
        Team team = findTeamOrThrow(teamId);
        validateOwner(team, ownerId);

        if (memberId.equals(ownerId)) {
            throw new BusinessException("Không thể xóa owner khỏi nhóm");
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)) {
            throw new BusinessException("Người dùng này không phải thành viên của nhóm");
        }

        teamMemberRepository.deleteByTeamIdAndUserId(teamId, memberId);
        log.info("Xóa userId={} khỏi nhóm id={}", memberId, teamId);
    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId, Long ownerId) {
        Team team = findTeamOrThrow(teamId);
        validateOwner(team, ownerId);
        teamRepository.delete(team);
        log.info("Xóa nhóm id={} bởi userId={}", teamId, ownerId);
    }

    // -----------------------------------------------------------------------

    private Team findTeamOrThrow(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
    }

    private void validateOwner(Team team, Long userId) {
        if (!team.getOwner().getId().equals(userId)) {
            throw new BusinessException("Chỉ owner nhóm mới có thể thực hiện thao tác này");
        }
    }

    private void validateAccess(Team team, Long userId) {
        boolean isMember = team.getMembers().stream()
                .anyMatch(tm -> tm.getUser().getId().equals(userId));
        boolean isOwner = team.getOwner().getId().equals(userId);
        if (!isOwner && !isMember) {
            throw new BusinessException("Bạn không có quyền truy cập nhóm này");
        }
    }
}
