package com.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.projecttracker.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin thông báo của người dùng.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private Long id;

    /** Loại thông báo: INVITATION_RECEIVED | INVITATION_ACCEPTED | INVITATION_DECLINED */
    private String type;

    /** Nội dung thông báo hiển thị */
    private String message;

    /**
     * ID tham chiếu.
     * - Với INVITATION_RECEIVED: là invitationId (để accept/decline)
     * - Với ACCEPTED/DECLINED: là invitationId
     */
    private Long referenceId;

    private Boolean isRead;

    private LocalDateTime createdAt;

    /**
     * Chuyển đổi từ Notification entity.
     */
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
