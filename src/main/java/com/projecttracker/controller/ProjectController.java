package com.projecttracker.controller;

import com.projecttracker.dto.request.ProjectRequest;
import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.entity.Project;
import com.projecttracker.security.UserPrincipal;
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

/**
 * REST Controller xử lý CRUD cho dự án.
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

    /**
     * Lấy danh sách dự án của user hiện tại (có phân trang).
     *
     * <p>GET /api/projects?page=0&size=10&sort=createdAt,desc</p>
     *
     * @param currentUser User đang đăng nhập
     * @param page        Số trang (bắt đầu từ 0)
     * @param size        Số item mỗi trang
     * @return Page<Project> với metadata phân trang
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách dự án", description = "Lấy tất cả dự án mà user tham gia")
    public ResponseEntity<ApiResponse<Page<Project>>> getMyProjects(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Project> projects = projectService.getUserProjects(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(projects, "Lấy danh sách dự án thành công"));
    }

    /**
     * Lấy chi tiết một dự án theo ID.
     *
     * <p>GET /api/projects/{id}</p>
     *
     * @param projectId   ID của dự án
     * @param currentUser User đang đăng nhập
     * @return Project entity
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "Lấy chi tiết dự án")
    public ResponseEntity<ApiResponse<Project>> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Project project = projectService.getProjectById(projectId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(project, "Lấy thông tin dự án thành công"));
    }

    /**
     * Tạo dự án mới.
     *
     * <p>POST /api/projects</p>
     *
     * @param request     Thông tin dự án
     * @param currentUser User đang đăng nhập (sẽ là owner)
     * @return 201 Created với Project entity
     */
    @PostMapping
    @Operation(summary = "Tạo dự án mới")
    public ResponseEntity<ApiResponse<Project>> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Project project = projectService.createProject(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(project, "Tạo dự án thành công"));
    }

    /**
     * Cập nhật thông tin dự án.
     *
     * <p>PUT /api/projects/{id}</p>
     *
     * @param projectId   ID dự án cần cập nhật
     * @param request     Thông tin cập nhật
     * @param currentUser User đang đăng nhập (phải là owner)
     * @return Project entity sau cập nhật
     */
    @PutMapping("/{projectId}")
    @Operation(summary = "Cập nhật dự án")
    public ResponseEntity<ApiResponse<Project>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Project updated = projectService.updateProject(projectId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật dự án thành công"));
    }

    /**
     * Xóa dự án.
     *
     * <p>DELETE /api/projects/{id}</p>
     *
     * @param projectId   ID dự án cần xóa
     * @param currentUser User đang đăng nhập (phải là owner)
     * @return 200 OK không có data
     */
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Xóa dự án")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        projectService.deleteProject(projectId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Xóa dự án thành công"));
    }
}
