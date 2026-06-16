package com.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.projecttracker.entity.User;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO trả về thông tin user khi tìm kiếm để mời vào dự án.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSearchResponse {

    private Long id;

    private String username;

    private String fullName;

    private String email;

    private String avatarUrl;

    private String role;

    /**
     * Chuyển đổi từ User entity.
     */
    public static UserSearchResponse from(User user) {
        return UserSearchResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();
    }
}
