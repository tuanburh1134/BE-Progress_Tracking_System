package com.projecttracker.controller;

import com.projecttracker.dto.request.InviteMemberRequest;
import com.projecttracker.dto.request.ProjectRequest;
import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.InvitationResponse;
import com.projecttracker.dto.response.ProjectResponse;
import com.projecttracker.dto.response.UserSearchResponse;
import com.projecttracker.security.UserPrincipal;
import com.projecttracker.service.InvitationService;
import com.projecttracker.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller xử lý CRUD cho dự án và quản lý thành viên.
 *
 * <p>Tất cả endpoint yêu cầu JWT token (Bearer Authentication).</p>
 * <p>Base URL: /api/projects</p>
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Quản lý dự án")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final InvitationService invitationService;

    /**
     * Lấy danh sách dự án của user hiện tại (có phân trang).
     *
     * <p>GET /api/projects?page=0&size=10</p>
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách dự án", description = "Lấy tất cả dự án mà user tham gia")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getMyProjects(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProjectResponse> projects = projectService.getUserProjects(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(projects, "Lấy danh sách dự án thành công"));
    }
    /**
     * Lấy danh sách thùng rác của user hiện tại (có phân trang).
     *
     * <p>GET /api/trash?page=0&size=10</p>
     */
    @GetMapping("/trash")
    @Operation(summary = "Lấy danh sách dự án trong Thùng rác")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getDeletedProjects(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by("deletedAt").descending());

        Page<ProjectResponse> projects =
                projectService.getDeletedProjects(currentUser.getId(), pageable);

        return ResponseEntity.ok(
                ApiResponse.success(projects, "Lấy danh sách thùng rác thành công"));
    }    
    /**
     * Lấy chi tiết một dự án theo ID.
     *
     * <p>GET /api/projects/{id}</p>
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "Lấy chi tiết dự án")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ProjectResponse project = projectService.getProjectById(projectId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(project, "Lấy thông tin dự án thành công"));
    }

    /**
     * Tạo dự án mới.
     *
     * <p>POST /api/projects</p>
     */
    @PostMapping
    @Operation(summary = "Tạo dự án mới")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ProjectResponse project = projectService.createProject(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(project, "Tạo dự án thành công"));
    }

    /**
     * Cập nhật thông tin dự án.
     *
     * <p>PUT /api/projects/{id}</p>
     */
    @PutMapping("/{projectId}")
    @Operation(summary = "Cập nhật dự án")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ProjectResponse updated = projectService.updateProject(projectId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật dự án thành công"));
    }

    /**
     * Xóa dự án.
     *
     * <p>DELETE /api/projects/{id}</p>
     */
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Xóa dự án")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        projectService.deleteProject(projectId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Xóa dự án thành công"));
    }
    /**
     * Khôi phục dự án từ Thùng rác.
     *
     * <p>PUT /api/projects/{id}/restore</p>
     */
    @PutMapping("/{projectId}/restore")
    @Operation(summary = "Khôi phục dự án")
    public ResponseEntity<ApiResponse<Void>> restoreProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        projectService.restoreProject(projectId, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Khôi phục dự án thành công"));
    }

    /**
     * Xóa vĩnh viễn dự án khỏi database.
     *
     * <p>DELETE /api/projects/{id}/permanent</p>
     */
    @DeleteMapping("/{projectId}/permanent")
    @Operation(summary = "Xóa vĩnh viễn dự án")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        projectService.permanentlyDeleteProject(projectId, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Đã xóa vĩnh viễn dự án"));
    }    
    // =========================================================================
    // Member management endpoints
    // =========================================================================

    /**
     * Lấy danh sách thành viên của dự án.
     *
     * <p>GET /api/projects/{id}/members</p>
     */
    @GetMapping("/{projectId}/members")
    @Operation(summary = "Lấy danh sách thành viên dự án")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> getMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<UserSearchResponse> members = projectService.getMembers(projectId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(members, "Lấy danh sách thành viên thành công"));
    }

    /**
     * Lấy danh sách lời mời đang PENDING của dự án (chỉ owner xem được).
     *
     * <p>GET /api/projects/{id}/invitations/pending</p>
     */
    @GetMapping("/{projectId}/invitations/pending")
    @Operation(summary = "Lấy danh sách lời mời đang chờ xác nhận của dự án")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getPendingInvitations(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<InvitationResponse> invitations =
                invitationService.getPendingInvitationsByProject(projectId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(invitations, "Lấy danh sách lời mời thành công"));
    }

    /**
     * Mời thành viên vào dự án theo email (tạo lời mời chờ xác nhận).
     *
     * <p>POST /api/projects/{id}/members</p>
     */
    @PostMapping("/{projectId}/members")
    @Operation(summary = "Gửi lời mời tham gia dự án",
               description = "Tạo lời mời PENDING, người được mời cần xác nhận trước khi vào nhóm")
    public ResponseEntity<ApiResponse<InvitationResponse>> inviteMember(
            @PathVariable Long projectId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        InvitationResponse invitation = invitationService.sendInvitation(
                projectId, request.getEmail(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(invitation, "Đã gửi lời mời, đang chờ xác nhận"));
    }

    /**
     * Xóa thành viên khỏi dự án.
     *
     * <p>DELETE /api/projects/{id}/members/{memberId}</p>
     */
    @DeleteMapping("/{projectId}/members/{memberId}")
    @Operation(summary = "Xóa thành viên khỏi dự án")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        projectService.removeMember(projectId, memberId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa thành viên khỏi dự án"));
    }

    /**
     * Nâng/hạ quyền thành viên trong dự án.
     *
     * <p>PUT /api/projects/{id}/members/{memberId}/role?role=MANAGER</p>
     */
    @PutMapping("/{projectId}/members/{memberId}/role")
    @Operation(summary = "Cập nhật vai trò/quyền của thành viên trong dự án")
    public ResponseEntity<ApiResponse<UserSearchResponse>> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestParam String role,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UserSearchResponse updatedMember = projectService.updateMemberRole(projectId, memberId, role, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(updatedMember, "Cập nhật vai trò thành viên thành công"));
    }
}
