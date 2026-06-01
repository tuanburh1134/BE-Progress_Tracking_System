package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho một task (công việc) trong dự án.
 *
 * <p>Task là đơn vị công việc nhỏ nhất trong hệ thống. Mỗi task thuộc một dự án,
 * có thể được phân công cho một thành viên và có trạng thái trên Kanban board.</p>
 *
 * <p>Dữ liệu task (thời gian ước tính, thực tế, trạng thái) là đầu vào chính
 * cho module AI dự đoán tiến độ.</p>
 */
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_project_id", columnList = "project_id"),
        @Index(name = "idx_tasks_assignee_id", columnList = "assignee_id"),
        @Index(name = "idx_tasks_status", columnList = "status"),
        @Index(name = "idx_tasks_deadline", columnList = "deadline")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tiêu đề ngắn gọn của task */
    @Column(name = "title", nullable = false, length = 300)
    private String title;

    /** Mô tả chi tiết, yêu cầu, tiêu chí hoàn thành */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Trạng thái hiện tại trên Kanban board */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    /** Mức độ ưu tiên */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    @Builder.Default
    private Project.Priority priority = Project.Priority.MEDIUM;

    /** Deadline của task */
    @Column(name = "deadline")
    private LocalDate deadline;

    /**
     * Thời gian ước tính để hoàn thành (đơn vị: giờ).
     * Dùng để AI so sánh với thời gian thực tế.
     */
    @Column(name = "estimated_hours")
    private Double estimatedHours;

    /**
     * Thời gian thực tế đã làm (đơn vị: giờ).
     * Được cập nhật khi task hoàn thành.
     */
    @Column(name = "actual_hours")
    private Double actualHours;

    /** Ngày bắt đầu làm task */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Ngày hoàn thành thực tế */
    @Column(name = "completed_date")
    private LocalDate completedDate;

    /** Thứ tự hiển thị trong cùng một cột Kanban */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

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

    /** Dự án chứa task này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Người được phân công thực hiện task */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /** Người tạo task */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // -----------------------------------------------------------------------
    // Enum
    // -----------------------------------------------------------------------

    /** Trạng thái của task trên Kanban board */
    public enum TaskStatus {
        TODO,        // Chưa bắt đầu
        IN_PROGRESS, // Đang thực hiện
        IN_REVIEW,   // Đang review/kiểm tra
        DONE,        // Hoàn thành
        BLOCKED      // Bị chặn (cần giải quyết dependency)
    }
}
