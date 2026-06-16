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
     * Lấy danh sách dự án của user hiện tại (có phân trang).
     *
     * @param userId   ID của user
     * @param pageable Thông tin phân trang và sort
     * @return Page<ProjectResponse> kết quả đã được convert sang DTO
     */
    Page<ProjectResponse> getUserProjects(Long userId, Pageable pageable);

    /**
     * Lấy thông tin chi tiết một dự án.
     *
     * @param projectId ID dự án
     * @param userId    ID user đang request (để kiểm tra quyền truy cập)
     * @return ProjectResponse DTO
     */
    ProjectResponse getProjectById(Long projectId, Long userId);

    /**
     * Tạo dự án mới.
     *
     * @param request Thông tin dự án cần tạo
     * @param ownerId ID của người tạo (owner)
     * @return ProjectResponse DTO của dự án vừa tạo
     */
    ProjectResponse createProject(ProjectRequest request, Long ownerId);

    /**
     * Cập nhật thông tin dự án.
     *
     * @param projectId ID dự án cần cập nhật
     * @param request   Thông tin cập nhật
     * @param userId    ID user đang request (kiểm tra quyền)
     * @return ProjectResponse DTO sau cập nhật
     */
    ProjectResponse updateProject(Long projectId, ProjectRequest request, Long userId);

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

    // -----------------------------------------------------------------------
    // Member management
    // -----------------------------------------------------------------------

    /**
     * Lấy danh sách thành viên của dự án.
     *
     * @param projectId ID dự án
     * @param userId    ID user đang request (phải là owner hoặc member)
     * @return Danh sách UserSearchResponse
     */
    List<UserSearchResponse> getMembers(Long projectId, Long userId);

    /**
     * Mời thành viên vào dự án theo email.
     *
     * @param projectId     ID dự án
     * @param email         Email của người được mời
     * @param currentUserId ID của người mời (phải là owner)
     * @return UserSearchResponse của thành viên vừa được thêm
     */
    UserSearchResponse addMember(Long projectId, String email, Long currentUserId);

    /**
     * Xóa thành viên khỏi dự án.
     *
     * @param projectId     ID dự án
     * @param memberId      ID của thành viên cần xóa
     * @param currentUserId ID của người thực hiện (phải là owner)
     */
    void removeMember(Long projectId, Long memberId, Long currentUserId);
}
