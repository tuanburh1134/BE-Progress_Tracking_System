package com.projecttracker.service;

import com.projecttracker.dto.request.ProjectRequest;
import com.projecttracker.dto.response.ProjectResponse;
import com.projecttracker.dto.response.UserSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface định nghĩa các thao tác quản lý dự án.
 */
public interface ProjectService {

    /**
     * Lấy danh sách dự án của user hiện tại (không bao gồm dự án đã xóa).
     *
     * @param userId   ID của user
     * @param pageable Thông tin phân trang
     * @return Danh sách dự án
     */
    Page<ProjectResponse> getUserProjects(Long userId, Pageable pageable);

    /**
     * Lấy danh sách dự án trong Thùng rác.
     *
     * @param userId   ID của user
     * @param pageable Thông tin phân trang
     * @return Danh sách dự án đã xóa
     */
    Page<ProjectResponse> getDeletedProjects(Long userId, Pageable pageable);

    /**
     * Lấy thông tin chi tiết một dự án.
     *
     * @param projectId ID dự án
     * @param userId ID user đang request
     * @return ProjectResponse
     */
    ProjectResponse getProjectById(Long projectId, Long userId);

    /**
     * Tạo dự án mới.
     *
     * @param request Thông tin dự án
     * @param ownerId Chủ sở hữu
     * @return ProjectResponse
     */
    ProjectResponse createProject(ProjectRequest request, Long ownerId);

    /**
     * Cập nhật dự án.
     *
     * @param projectId ID dự án
     * @param request Dữ liệu cập nhật
     * @param userId User thực hiện
     * @return ProjectResponse
     */
    ProjectResponse updateProject(Long projectId,
                                  ProjectRequest request,
                                  Long userId);

    /**
     * Xóa mềm dự án (đưa vào Thùng rác).
     *
     * @param projectId ID dự án
     * @param userId Chủ sở hữu
     */
    void deleteProject(Long projectId, Long userId);

    /**
     * Khôi phục dự án từ Thùng rác.
     *
     * @param projectId ID dự án
     * @param userId Chủ sở hữu
     */
    void restoreProject(Long projectId, Long userId);

    /**
     * Xóa vĩnh viễn dự án khỏi database.
     *
     * @param projectId ID dự án
     * @param userId Chủ sở hữu
     */
    void permanentlyDeleteProject(Long projectId, Long userId);

    /**
     * Cập nhật tiến độ dự án.
     *
     * @param projectId ID dự án
     */
    void recalculateProgress(Long projectId);

    // -----------------------------------------------------------------------
    // Member management
    // -----------------------------------------------------------------------

    /**
     * Danh sách thành viên.
     */
    List<UserSearchResponse> getMembers(Long projectId, Long userId);

    /**
     * Thêm thành viên.
     */
    UserSearchResponse addMember(Long projectId,
                                 String email,
                                 Long currentUserId);

    /**
     * Xóa thành viên.
     */
    void removeMember(Long projectId,
                      Long memberId,
                      Long currentUserId);
}