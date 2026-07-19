package com.projecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleRequest {

    @NotBlank
    private String role; // ADMIN, PROJECT_MANAGER, MEMBER
}
