package com.projecttracker.service;

import com.projecttracker.dto.response.NotificationResponse;

import java.util.List;

/**
 * Service quản lý thông báo trong hệ thống.
 */
public interface NotificationService {

    /**
     * Tạo thông báo mới đồng thời tự động gửi Email thông báo tới người nhận.
     */
    com.projecttracker.entity.Notification createNotification(
            com.projecttracker.entity.User recipient,
            com.projecttracker.entity.Notification.NotificationType type,
            String message,
            Long referenceId
    );

    /**
     * Lấy tất cả thông báo của user (mới nhất trước).
     *
     * @param userId ID user
     * @return Danh sách thông báo
     */
    List<NotificationResponse> getNotifications(Long userId);

    /**
     * Đếm số thông báo chưa đọc.
     *
     * @param userId ID user
     * @return Số lượng chưa đọc
     */
    long getUnreadCount(Long userId);

    /**
     * Đánh dấu một thông báo là đã đọc.
     *
     * @param notificationId ID thông báo
     * @param userId         ID user (để kiểm tra quyền)
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc.
     *
     * @param userId ID user
     */
    void markAllAsRead(Long userId);
}
