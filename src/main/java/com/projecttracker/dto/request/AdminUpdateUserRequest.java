package com.projecttracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserRequest {

    @Size(min = 3, max = 50)
    private String username;

    @Email
    private String email;

    private String fullName;

    private String avatarUrl;

    private Boolean isActive;
}
