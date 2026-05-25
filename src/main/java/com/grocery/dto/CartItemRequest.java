package com.grocery.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull
    private Long productId;
    @Min(1)
    private int quantity;
}
