package com.projecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** DTO cho request tạo nhóm. */
@Getter
@Setter
public class TeamRequest {

    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(min = 2, max = 100, message = "Tên nhóm phải từ 2-100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không quá 500 ký tự")
    private String description;
}
