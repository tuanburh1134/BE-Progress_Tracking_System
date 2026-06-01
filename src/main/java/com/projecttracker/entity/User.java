package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity đại diện cho người dùng trong hệ thống.
 *
 * <p>Mỗi user có thể là thành viên của nhiều dự án và được phân công nhiều task.
 * Role xác định quyền hạn trong hệ thống (ADMIN, PROJECT_MANAGER, MEMBER).</p>
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_username", columnList = "username", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên đăng nhập, duy nhất trong hệ thống */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /** Email, dùng để đăng nhập và thông báo */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /** Mật khẩu đã mã hóa bằng BCrypt */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Họ và tên đầy đủ */
    @Column(name = "full_name", length = 100)
    private String fullName;

    /** URL ảnh đại diện */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /** Vai trò trong hệ thống */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.MEMBER;

    /** Trạng thái tài khoản (active/inactive) */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Thời gian tạo tài khoản, tự động gán */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời gian cập nhật cuối, tự động gán */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // -----------------------------------------------------------------------
    // Relationships
    // -----------------------------------------------------------------------

    /** Các dự án mà user này là thành viên */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProjectMember> projectMemberships = new HashSet<>();

    /** Các task được phân công cho user này */
    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Task> assignedTasks = new HashSet<>();

    // -----------------------------------------------------------------------
    // Enum
    // -----------------------------------------------------------------------

    /** Các vai trò người dùng trong hệ thống */
    public enum UserRole {
        ADMIN,           // Quản trị hệ thống
        PROJECT_MANAGER, // Quản lý dự án
        MEMBER           // Thành viên thông thường
    }
}
