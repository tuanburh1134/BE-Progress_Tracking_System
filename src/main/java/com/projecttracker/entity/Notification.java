package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity lưu thông báo trong hệ thống.
 *
 * <p>Dùng cho các sự kiện: nhận lời mời, được chấp nhận, bị từ chối.</p>
 */
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notif_recipient_read", columnList = "recipient_id, is_read"),
                @Index(name = "idx_notif_created_at",     columnList = "created_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Loại thông báo */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    /** Nội dung thông báo hiển thị cho người dùng */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** ID tham chiếu (vd: invitationId) để frontend xử lý action */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Đã đọc chưa */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /** Thời gian tạo thông báo */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // -----------------------------------------------------------------------
    // Relationships
    // -----------------------------------------------------------------------

    /** Người nhận thông báo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // -----------------------------------------------------------------------
    // Enum
    // -----------------------------------------------------------------------

    public enum NotificationType {
        INVITATION_RECEIVED,  // Bạn nhận được lời mời vào dự án
        INVITATION_ACCEPTED,  // Người được mời đã chấp nhận
        INVITATION_DECLINED   // Người được mời đã từ chối
    }
}
