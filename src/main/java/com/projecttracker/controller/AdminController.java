package com.projecttracker.controller;

import com.projecttracker.dto.request.AdminCreateUserRequest;
import com.projecttracker.dto.request.AdminUpdateUserRequest;
import com.projecttracker.dto.request.ChangeRoleRequest;
import com.projecttracker.dto.response.AdminDashboardResponse;
import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.UserAdminResponse;
import com.projecttracker.entity.User;
import com.projecttracker.repository.ProjectRepository;
import com.projecttracker.repository.TaskRepository;
import com.projecttracker.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller cho các chức năng quản trị hệ thống (ADMIN).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminStats() {
        long totalUsers = userRepository.count();
        long totalProjects = projectRepository.count();
        long totalTasks = taskRepository.count();
        long completed = taskRepository.countByStatus(com.projecttracker.entity.Task.TaskStatus.DONE);
        long inProgress = taskRepository.countByStatus(com.projecttracker.entity.Task.TaskStatus.IN_PROGRESS);
        long overdue = taskRepository.countOverdueTasks(LocalDate.now());

        AdminDashboardResponse resp = AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .completedTasks(completed)
                .inProgressTasks(inProgress)
                .overdueTasks(overdue)
                .build();

        return ResponseEntity.ok(ApiResponse.success(resp, "Lấy thống kê admin thành công"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pr);
        List<UserAdminResponse> items = users.stream().map(UserAdminResponse::from).collect(Collectors.toList());
        Page<UserAdminResponse> out = new PageImpl<>(items, pr, users.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(out, "Danh sách user"));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserAdminResponse>> createUser(@Valid @RequestBody AdminCreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Email đã tồn tại"));
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Username đã tồn tại"));
        }

        User u = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .avatarUrl(req.getAvatarUrl())
                .isActive(true)
                .build();

        // parse role if provided
        if (req.getRole() != null) {
            try {
                u.setRole(User.UserRole.valueOf(req.getRole()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        User saved = userRepository.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(UserAdminResponse.from(saved), "Tạo user thành công"));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(@PathVariable Long id,
                                                                     @Valid @RequestBody AdminUpdateUserRequest req) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();

        if (req.getUsername() != null) u.setUsername(req.getUsername());
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getFullName() != null) u.setFullName(req.getFullName());
        if (req.getAvatarUrl() != null) u.setAvatarUrl(req.getAvatarUrl());
        if (req.getIsActive() != null) u.setIsActive(req.getIsActive());

        User saved = userRepository.save(u);
        return ResponseEntity.ok(ApiResponse.success(UserAdminResponse.from(saved), "Cập nhật user thành công"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserAdminResponse>> changeRole(@PathVariable Long id,
                                                                     @Valid @RequestBody ChangeRoleRequest req) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        try {
            u.setRole(User.UserRole.valueOf(req.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Role không hợp lệ"));
        }
        User saved = userRepository.save(u);
        return ResponseEntity.ok(ApiResponse.success(UserAdminResponse.from(saved), "Đổi role thành công"));
    }

    @PutMapping("/users/{id}/lock")
    public ResponseEntity<ApiResponse<UserAdminResponse>> lockUnlock(@PathVariable Long id,
                                                                     @RequestParam boolean active) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        u.setIsActive(active);
        User saved = userRepository.save(u);
        String msg = active ? "Mở khóa tài khoản" : "Khóa tài khoản";
        return ResponseEntity.ok(ApiResponse.success(UserAdminResponse.from(saved), msg));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success((Void) null, "Đã xóa user"));
    }
}
