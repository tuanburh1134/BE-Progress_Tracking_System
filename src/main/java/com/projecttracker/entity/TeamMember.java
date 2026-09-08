package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho một thành viên trong nhóm.
 *
 * <p>Khi owner mời ai đó, một TeamMember PENDING được tạo.
 * Chỉ sau khi ACCEPTED, người đó mới chính thức là thành viên.</p>
 */
@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tm_team_user",
                columnNames = {"team_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_tm_user_status", columnList = "user_id, status"),
                @Index(name = "idx_tm_team_id",     columnList = "team_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Trạng thái lời mời */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TeamMemberStatus status = TeamMemberStatus.PENDING;

    /** Thời gian được mời */
    @CreatedDate
    @Column(name = "invited_at", nullable = false, updatable = false)
    private LocalDateTime invitedAt;

    /** Thời gian phản hồi */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // -----------------------------------------------------------------------
    // Relationships
    // -----------------------------------------------------------------------

    /** Nhóm */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Người được mời */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Người gửi lời mời */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    // -----------------------------------------------------------------------
    // Enum
    // -----------------------------------------------------------------------

    public enum TeamMemberStatus {
        PENDING,   // Chờ phản hồi
        ACCEPTED,  // Đã chấp nhận
        DECLINED   // Đã từ chối
    }
}
