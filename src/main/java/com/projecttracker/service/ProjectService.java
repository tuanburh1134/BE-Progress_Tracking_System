package com.projecttracker.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.projecttracker.dto.request.ProjectRequest;
import com.projecttracker.entity.Project;

/**
 * Service interface định nghĩa các thao tác quản lý dự án.
 */
public interface ProjectService {

    /**
     * Lấy danh sách dự án của user hiện tại (có phân trang).
     *
     * @param userId   ID của user
     * @param pageable Thông tin phân trang và sort
     * @return Page<Project> kết quả
     */
    Page<Project> getUserProjects(Long userId, Pageable pageable);

    /**
     * Lấy thông tin chi tiết một dự án.
     *
     * @param projectId ID dự án
     * @param userId    ID user đang request (để kiểm tra quyền truy cập)
     * @return Project entity
     */
    Project getProjectById(Long projectId, Long userId);

    /**
     * Tạo dự án mới.
     *
     * @param request Thông tin dự án cần tạo
     * @param ownerId ID của người tạo (owner)
     * @return Project entity vừa tạo
     */
    Project createProject(ProjectRequest request, Long ownerId);

    /**
     * Cập nhật thông tin dự án.
     *
     * @param projectId ID dự án cần cập nhật
     * @param request   Thông tin cập nhật
     * @param userId    ID user đang request (kiểm tra quyền)
     * @return Project entity sau cập nhật
     */
    Project updateProject(Long projectId, ProjectRequest request, Long userId);

    /**
     * Xóa dự án.
     *
     * @param projectId ID dự án cần xóa
     * @param userId    ID user đang request (phải là owner)
     */
    void deleteProject(Long projectId, Long userId);

    /**
     * Cập nhật tiến độ dự án dựa trên tỷ lệ task hoàn thành.
     * Được gọi tự động khi có task thay đổi trạng thái.
     *
     * @param projectId ID dự án cần tính lại tiến độ
     */
    void recalculateProgress(Long projectId);
}
