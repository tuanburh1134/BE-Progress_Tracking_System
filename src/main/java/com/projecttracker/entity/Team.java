package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho một Nhóm (Team) trong hệ thống.
 *
 * <p>Một nhóm có owner và danh sách thành viên với trạng thái lời mời.</p>
 */
@Entity
@Table(name = "teams",
        indexes = @Index(name = "idx_team_owner", columnList = "owner_id")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên nhóm */
    @Column(name = "name", nullable = false)
    private String name;

    /** Thời gian tạo */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // -----------------------------------------------------------------------
    // Relationships
    // -----------------------------------------------------------------------

    /** Chủ nhóm */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Danh sách thành viên (bao gồm cả PENDING) */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamMember> members = new ArrayList<>();
}
