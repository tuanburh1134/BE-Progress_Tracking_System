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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Quản lý thông báo")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lấy thông báo chưa đọc")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<NotificationResponse> notifications = notificationService.getUnread(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(notifications, "Lấy thông báo thành công"));
    }

    @GetMapping("/count")
    @Operation(summary = "Đếm số thông báo chưa đọc")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnread(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        long count = notificationService.countUnread(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count), "OK"));
    }

    @PutMapping("/read-all")
    @Transactional
    @Operation(summary = "Đánh dấu tất cả thông báo là đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        notificationService.markAllRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả là đã đọc"));
    }
}
