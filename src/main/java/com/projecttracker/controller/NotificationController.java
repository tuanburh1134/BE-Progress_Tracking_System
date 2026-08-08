package com.projecttracker.controller;

import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.NotificationResponse;
import com.projecttracker.security.UserPrincipal;
import com.projecttracker.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller quản lý thông báo.
 *
 * <p>Base URL: /api/notifications</p>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Quản lý thông báo người dùng")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Lấy tất cả thông báo của user hiện tại.
     *
     * <p>GET /api/notifications</p>
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<NotificationResponse> notifications =
                notificationService.getNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(notifications, "Lấy thông báo thành công"));
    }

    /**
     * Đếm số thông báo chưa đọc.
     *
     * <p>GET /api/notifications/unread-count</p>
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Đếm số thông báo chưa đọc")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count), "OK"));
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     *
     * <p>PATCH /api/notifications/{id}/read</p>
     */
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Đánh dấu thông báo đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        notificationService.markAsRead(notificationId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đọc"));
    }

    /**
     * Đánh dấu tất cả thông báo là đã đọc.
     *
     * <p>PATCH /api/notifications/read-all</p>
     */
    @PatchMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả đã đọc"));
    }
}
