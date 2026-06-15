package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity đại diện cho một dự án trong hệ thống.
 *
 * <p>Một dự án có nhiều task, nhiều thành viên tham gia và có các thông tin
 * về timeline (ngày bắt đầu, deadline). AI module sẽ dựa vào dữ liệu dự án
 * để dự đoán nguy cơ trễ hạn.</p>
 */
@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_projects_owner_id", columnList = "owner_id"),
        @Index(name = "idx_projects_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên dự án */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Mã định danh ngắn gọn của dự án (ví dụ: ECOMM-A4) */
    @Column(name = "project_code", length = 50)
    private String projectCode;

    /** Mô tả chi tiết dự án */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Ngày bắt đầu dự án */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Deadline của dự án */
    @Column(name = "deadline")
    private LocalDate deadline;

    /** Trạng thái hiện tại của dự án */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PLANNING;

    /** Mức độ ưu tiên của dự án */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    /**
     * Tiến độ tổng thể (0-100%), được tính tự động dựa trên
     * tỷ lệ task đã hoàn thành / tổng số task.
     */
    @Column(name = "progress", nullable = false)
    @Builder.Default
    private Integer progress = 0;

    /** Thời gian tạo, tự động gán */
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

        
    /** Người tạo/quản lý dự án */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Danh sách task thuộc dự án này */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    /** Danh sách thành viên dự án */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProjectMember> members = new HashSet<>();

    // -----------------------------------------------------------------------
    // Enums
    // -----------------------------------------------------------------------

        
    /** Trạng thái của dự án */
    public enum ProjectStatus {
        PLANNING,    // Đang lên kế hoạch
        IN_PROGRESS, // Đang thực hiện
        ON_HOLD,     // Tạm dừng
        COMPLETED,   // Hoàn thành
        CANCELLED    // Đã hủy
    }

    /** Mức độ ưu tiên */
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
