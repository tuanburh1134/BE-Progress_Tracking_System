package com.projecttracker.service;

import com.projecttracker.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    /** Lấy danh sách thông báo chưa đọc của user */
    List<NotificationResponse> getUnread(Long userId);

    /** Đếm thông báo chưa đọc */
    long countUnread(Long userId);

    /** Đánh dấu tất cả là đã đọc */
    void markAllRead(Long userId);

    /** Tạo thông báo mới */
    void createNotification(Long userId, String type, String message, Long refId);
}
