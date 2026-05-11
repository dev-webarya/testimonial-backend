package com.blogapp.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {
    private String oldPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
