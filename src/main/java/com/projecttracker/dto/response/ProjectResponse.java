package com.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.projecttracker.entity.Project;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin dự án — không lộ entity trực tiếp ra ngoài.
 *
 * <p>Bao gồm taskCount và memberCount được tính sẵn để frontend
 * hiển thị trên card mà không cần thêm request.</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponse {

    private Long id;

    private String name;

    private String projectCode;

    private String description;

    /** Trạng thái kỹ thuật (enum name) */
    private String status;

    /** Nhãn tiếng Việt hiển thị trên UI */
    private String statusLabel;

    /** Mức ưu tiên kỹ thuật */
    private String priority;


    private String sdlc;

    /** Nhãn tiếng Việt */
    private String priorityLabel;

    /** Tiến độ 0-100% */
    private Integer progress;

    private LocalDate startDate;

    private LocalDate deadline;

    /** Số lượng task trong dự án */
    private int taskCount;

    /** Số lượng thành viên (bao gồm cả owner) */
    private int memberCount;

    /** Họ tên người tạo/owner */
    private String ownerName;

    private Long ownerId;

    private LocalDateTime createdAt;

    private String githubLink;

    // -----------------------------------------------------------------------
    // Static factory — convert từ entity
    // -----------------------------------------------------------------------

    /**
     * Chuyển đổi Project entity sang ProjectResponse DTO.
     *
     * @param project Entity cần convert
     * @return ProjectResponse đã được điền đủ thông tin
     */
    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .projectCode(project.getProjectCode())
                .description(project.getDescription())
                .status(project.getStatus().name())
                .statusLabel(toStatusLabel(project.getStatus()))
                .priority(project.getPriority().name())
                .sdlc(project.getSdlc().name())
                .priorityLabel(toPriorityLabel(project.getPriority()))
                .progress(project.getProgress())
                .startDate(project.getStartDate())
                .deadline(project.getDeadline())
                .taskCount(project.getTasks() != null ? project.getTasks().size() : 0)
                .memberCount(project.getMembers() != null ? project.getMembers().size() + 1 : 1) // +1 owner
                .ownerName(project.getOwner() != null ? project.getOwner().getFullName() : null)
                .ownerId(project.getOwner() != null ? project.getOwner().getId() : null)
                .createdAt(project.getCreatedAt())
                .githubLink(project.getGithubLink())
                .build();
    }

    // -----------------------------------------------------------------------
    // Label helpers
    // -----------------------------------------------------------------------

    private static String toStatusLabel(Project.ProjectStatus status) {
        return switch (status) {
            case PLANNING    -> "Lên kế hoạch";
            case IN_PROGRESS -> "Đang thực hiện";
            case ON_HOLD     -> "Tạm dừng";
            case COMPLETED   -> "Hoàn thành";
            case CANCELLED   -> "Đã hủy";
        };
    }

    private static String toPriorityLabel(Project.Priority priority) {
        return switch (priority) {
            case LOW      -> "Thấp";
            case MEDIUM   -> "Trung bình";
            case HIGH     -> "Cao";
            case CRITICAL -> "Khẩn cấp";
        };
    }
}
