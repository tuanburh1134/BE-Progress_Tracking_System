package com.projecttracker.service.impl;

import com.projecttracker.dto.response.NotificationResponse;
import com.projecttracker.entity.Notification;
import com.projecttracker.exception.BusinessException;
import com.projecttracker.exception.ResourceNotFoundException;
import com.projecttracker.repository.NotificationRepository;
import com.projecttracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Triển khai NotificationService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final com.projecttracker.service.EmailService emailService;

    @Override
    @Transactional
    public Notification createNotification(com.projecttracker.entity.User recipient, Notification.NotificationType type, String message, Long referenceId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Gửi email thông báo song song (Async)
        if (recipient != null && recipient.getEmail() != null) {
            String title = switch (type) {
                case INVITATION_RECEIVED -> "Lời mời tham gia dự án mới";
                case INVITATION_ACCEPTED -> "Lời mời dự án đã được chấp nhận";
                case INVITATION_DECLINED -> "Lời mời dự án bị từ chối";
                case TEAM_INVITATION_RECEIVED -> "Lời mời tham gia nhóm mới";
                case TEAM_INVITATION_ACCEPTED -> "Lời mời nhóm đã được chấp nhận";
                case TEAM_INVITATION_DECLINED -> "Lời mời nhóm bị từ chối";
                default -> "Thông báo mới";
            };

            emailService.sendNotificationEmail(recipient.getEmail(), title, message);
        }

        return savedNotification;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new BusinessException("Không có quyền đánh dấu thông báo này");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.debug("Đã đánh dấu tất cả thông báo đã đọc cho userId={}", userId);
    }
}
