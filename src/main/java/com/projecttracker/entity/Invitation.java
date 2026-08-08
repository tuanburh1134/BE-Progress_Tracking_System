package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho lời mời tham gia dự án.
 *
 * <p>Khi owner mời ai đó, một Invitation PENDING được tạo ra.
 * Người được mời có thể chấp nhận hoặc từ chối.
 * Chỉ sau khi chấp nhận, ProjectMember mới được tạo.</p>
 */
@Entity
@Table(name = "invitations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inv_project_invitee",
                columnNames = {"project_id", "invitee_id"}
        ),
        indexes = {
                @Index(name = "idx_inv_invitee_status", columnList = "invitee_id, status"),
                @Index(name = "idx_inv_project_id",     columnList = "project_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Trạng thái lời mời */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    /** Thời gian tạo lời mời, tự động gán */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời gian người được mời phản hồi */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // -----------------------------------------------------------------------
    // Relationships
    // -----------------------------------------------------------------------

    /** Dự án được mời vào */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Người gửi lời mời (owner/manager) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    /** Người được mời */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee;

    // -----------------------------------------------------------------------
    // Enum
    // -----------------------------------------------------------------------

    public enum InvitationStatus {
        PENDING,   // Chờ phản hồi
        ACCEPTED,  // Đã chấp nhận
        DECLINED   // Đã từ chối
    }
}
