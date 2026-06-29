package com.projecttracker.dto.response;

import com.projecttracker.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** DTO trả về thông tin thông báo. */
@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String message;
    private boolean read;
    private Long refId;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .message(n.getMessage())
                .read(Boolean.TRUE.equals(n.getIsRead()))
                .refId(n.getRefId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
