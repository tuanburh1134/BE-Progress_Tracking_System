package com.projecttracker.controller;

import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.UserAdminResponse;
import com.projecttracker.dto.response.UserSearchResponse;
import com.projecttracker.entity.User;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller xử lý các request liên quan đến User.
 *
 * <p>Base URL: /api/users</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Quản lý người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Danh sách người dùng", description = "Trả về danh sách người dùng hỗ trợ phân trang cho frontend")
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pr);
        List<UserAdminResponse> items = users.stream()
                .map(UserAdminResponse::from)
                .collect(Collectors.toList());
        Page<UserAdminResponse> out = new PageImpl<>(items, pr, users.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(out, "Danh sách người dùng"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy người dùng"));
        }
        return ResponseEntity.ok(ApiResponse.success(UserAdminResponse.from(user), "Chi tiết người dùng"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminResponse>> createUser(@Valid @RequestBody com.projecttracker.dto.request.AdminCreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Email đã tồn tại"));
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username đã tồn tại"));
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(req.getPassword()))
                .fullName(req.getFullName())
                .avatarUrl(req.getAvatarUrl())
                .role(req.getRole() != null ? User.UserRole.valueOf(req.getRole().toUpperCase()) : User.UserRole.MEMBER)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(UserAdminResponse.from(saved), "Tạo người dùng thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(@PathVariable Long id,
                                                                     @Valid @RequestBody com.projecttracker.dto.request.AdminUpdateUserRequest req) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy người dùng"));
        }

        if (req.getUsername() != null) user.setUsername(req.getUsername());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        if (req.getIsActive() != null) user.setIsActive(req.getIsActive());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserAdminResponse.from(saved), "Cập nhật người dùng thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy người dùng"));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa người dùng thành công"));
    }

    /**
     * Tìm kiếm user theo email để mời vào dự án.
     *
     * <p>GET /api/users/search?email=abc&size=5</p>
     *
     * <p>Loại trừ chính user đang đăng nhập khỏi kết quả.</p>
     *
     * @param email       Chuỗi email cần tìm kiếm (tìm theo LIKE)
     * @param size        Số kết quả tối đa trả về (mặc định 5)
     * @param currentUser User đang đăng nhập
     * @return Danh sách user phù hợp (không bao gồm currentUser)
     */
    @GetMapping("/search")
    @Operation(
            summary = "Tìm kiếm user theo email",
            description = "Tìm kiếm user để mời vào dự án. Trả về tối đa 5 kết quả phù hợp."
    )
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
            @RequestParam String email,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Yêu cầu tối thiểu 2 ký tự để tìm kiếm
        if (email == null || email.trim().length() < 2) {
            return ResponseEntity.ok(ApiResponse.success(List.of(), "Nhập ít nhất 2 ký tự để tìm kiếm"));
        }

        List<User> users = userRepository.searchByEmailExcluding(
                email.trim(),
                currentUser.getId(),
                PageRequest.of(0, Math.min(size, 10))
        );

        List<UserSearchResponse> result = users.stream()
                .map(UserSearchResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(result, "Tìm kiếm thành công"));
    }
}
