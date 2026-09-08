package com.projecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body để tạo nhóm mới.
 */
@Data
public class CreateTeamRequest {

    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(max = 100, message = "Tên nhóm không được vượt quá 100 ký tự")
    private String name;
}
