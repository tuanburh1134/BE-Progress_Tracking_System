package com.projecttracker.controller;

import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.UserSearchResponse;
import com.projecttracker.entity.User;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * Lấy thông tin hồ sơ của user hiện tại.
     *
     * <p>GET /api/users/profile</p>
     */
    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin cá nhân hiện tại")
    public ResponseEntity<ApiResponse<UserSearchResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new com.projecttracker.exception.ResourceNotFoundException("User", "id", currentUser.getId()));
                
        return ResponseEntity.ok(ApiResponse.success(UserSearchResponse.from(user), "Lấy thông tin cá nhân thành công"));
    }

    /**
     * Cập nhật thông tin hồ sơ cá nhân.
     *
     * <p>PUT /api/users/profile</p>
     */
    @PutMapping("/profile")
    @Operation(summary = "Cập nhật thông tin cá nhân")
    public ResponseEntity<ApiResponse<UserSearchResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @jakarta.validation.Valid @RequestBody com.projecttracker.dto.request.UpdateProfileRequest request) {
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new com.projecttracker.exception.ResourceNotFoundException("User", "id", currentUser.getId()));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getGithubUsername() != null) {
            user.setGithubUsername(request.getGithubUsername().trim().isEmpty() ? null : request.getGithubUsername().trim());
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserSearchResponse.from(updatedUser), "Cập nhật thông tin cá nhân thành công"));
    }
}
