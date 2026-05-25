package com.grocery.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String otp;
    @Size(min = 6)
    private String newPassword;
}
