package com.grocery.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AuthRequest {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password;
}
