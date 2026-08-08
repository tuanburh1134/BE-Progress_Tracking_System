package com.projecttracker.service.impl;

import com.projecttracker.dto.response.InvitationResponse;
import com.projecttracker.entity.*;
import com.projecttracker.exception.BusinessException;
import com.projecttracker.exception.ResourceNotFoundException;
import com.projecttracker.repository.*;
import com.projecttracker.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Triển khai InvitationService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository    invitationRepository;
    private final ProjectRepository       projectRepository;
    private final UserRepository          userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationRepository  notificationRepository;

    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public InvitationResponse sendInvitation(Long projectId, String inviteeEmail, Long inviterId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Chỉ owner mới được mời
        if (!project.getOwner().getId().equals(inviterId)) {
            throw new BusinessException("Chỉ owner mới có thể mời thành viên");
        }

        User invitee = userRepository.findByEmail(inviteeEmail)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với email: " + inviteeEmail));

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", inviterId));

        // Không mời chính mình
        if (invitee.getId().equals(inviterId)) {
            throw new BusinessException("Bạn không thể mời chính mình vào dự án");
        }

        // Đã là thành viên chính thức rồi
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, invitee.getId())) {
            throw new BusinessException("Người dùng này đã là thành viên chính thức của dự án");
        }

        // Đã có lời mời PENDING rồi
        if (invitationRepository.existsByProjectIdAndInviteeIdAndStatus(
                projectId, invitee.getId(), Invitation.InvitationStatus.PENDING)) {
            throw new BusinessException("Đã gửi lời mời cho người này, đang chờ họ xác nhận");
        }

        // Tạo Invitation
        Invitation invitation = Invitation.builder()
                .project(project)
                .inviter(inviter)
                .invitee(invitee)
                .status(Invitation.InvitationStatus.PENDING)
                .build();
        invitation = invitationRepository.save(invitation);

        // Tạo Notification cho invitee
        String message = String.format("'%s' đã mời bạn tham gia dự án '%s'",
                inviter.getFullName(), project.getName());
        Notification notification = Notification.builder()
                .recipient(invitee)
                .type(Notification.NotificationType.INVITATION_RECEIVED)
                .message(message)
                .referenceId(invitation.getId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        log.info("Đã gửi lời mời từ userId={} đến email={} vào dự án id={}",
                inviterId, inviteeEmail, projectId);

        return InvitationResponse.from(invitation);
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void acceptInvitation(Long invitationId, Long userId) {
        Invitation invitation = getInvitationForUser(invitationId, userId);

        // Kiểm tra chưa là thành viên (tránh race condition)
        if (projectMemberRepository.existsByProjectIdAndUserId(
                invitation.getProject().getId(), userId)) {
            throw new BusinessException("Bạn đã là thành viên của dự án này rồi");
        }

        // Tạo ProjectMember chính thức
        ProjectMember member = ProjectMember.builder()
                .project(invitation.getProject())
                .user(invitation.getInvitee())
                .role(ProjectMember.ProjectRole.MEMBER)
                .build();
        projectMemberRepository.save(member);

        // Cập nhật trạng thái invitation
        invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Thông báo cho owner
        String message = String.format("'%s' đã chấp nhận lời mời tham gia dự án '%s'",
                invitation.getInvitee().getFullName(), invitation.getProject().getName());
        Notification notification = Notification.builder()
                .recipient(invitation.getInviter())
                .type(Notification.NotificationType.INVITATION_ACCEPTED)
                .message(message)
                .referenceId(invitationId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        log.info("userId={} đã chấp nhận lời mời invitationId={}", userId, invitationId);
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void declineInvitation(Long invitationId, Long userId) {
        Invitation invitation = getInvitationForUser(invitationId, userId);

        // Cập nhật trạng thái
        invitation.setStatus(Invitation.InvitationStatus.DECLINED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Thông báo cho owner
        String message = String.format("'%s' đã từ chối lời mời tham gia dự án '%s'",
                invitation.getInvitee().getFullName(), invitation.getProject().getName());
        Notification notification = Notification.builder()
                .recipient(invitation.getInviter())
                .type(Notification.NotificationType.INVITATION_DECLINED)
                .message(message)
                .referenceId(invitationId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        log.info("userId={} đã từ chối lời mời invitationId={}", userId, invitationId);
    }

    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getPendingInvitations(Long userId) {
        return invitationRepository
                .findByInviteeIdAndStatusOrderByCreatedAtDesc(userId, Invitation.InvitationStatus.PENDING)
                .stream()
                .map(InvitationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getPendingInvitationsByProject(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException("Chỉ owner mới có thể xem danh sách lời mời đang chờ");
        }

        return invitationRepository
                .findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, Invitation.InvitationStatus.PENDING)
                .stream()
                .map(InvitationResponse::from)
                .toList();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Invitation getInvitationForUser(Long invitationId, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", invitationId));

        if (!invitation.getInvitee().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền phản hồi lời mời này");
        }

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new BusinessException("Lời mời này đã được xử lý rồi");
        }

        return invitation;
    }
}
