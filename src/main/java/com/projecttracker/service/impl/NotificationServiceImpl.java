package com.projecttracker.service.impl;

import com.projecttracker.dto.response.NotificationResponse;
import com.projecttracker.entity.Notification;
import com.projecttracker.entity.User;
import com.projecttracker.repository.NotificationRepository;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnread(Long userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
        log.debug("Đánh dấu đã đọc tất cả thông báo cho userId={}", userId);
    }

    @Override
    @Transactional
    public void createNotification(Long userId, String type, String message, Long refId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification notification = Notification.builder()
                .user(user)
                .type(Notification.NotificationType.valueOf(type))
                .message(message)
                .isRead(false)
                .refId(refId)
                .build();

        notificationRepository.save(notification);
        log.debug("Tạo thông báo '{}' cho userId={}", type, userId);
    }
}
