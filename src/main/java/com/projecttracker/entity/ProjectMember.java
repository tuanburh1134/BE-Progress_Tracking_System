package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho mối quan hệ nhiều-nhiều giữa User và Project.
 *
 * <p>Lưu trữ thông tin về vai trò của một user trong một dự án cụ thể.
 * Một user có thể có vai trò khác nhau ở mỗi dự án khác nhau.</p>
 */
@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_user",
                columnNames = {"project_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_pm_project_id", columnList = "project_id"),
                @Index(name = "idx_pm_user_id", columnList = "user_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Vai trò của user trong dự án cụ thể này */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private ProjectRole role = ProjectRole.MEMBER;

    /** Thời gian tham gia dự án */
    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    // -----------------------------------------------------------------------
    // Relationships
    // -----------------------------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // -----------------------------------------------------------------------
    // Enum
    // -----------------------------------------------------------------------

    /** Vai trò của thành viên trong một dự án */
    public enum ProjectRole {
        OWNER,   // Người tạo/chủ dự án
        MANAGER, // Quản lý dự án
        MEMBER,  // Thành viên tham gia
        VIEWER   // Chỉ xem, không chỉnh sửa
    }
}
