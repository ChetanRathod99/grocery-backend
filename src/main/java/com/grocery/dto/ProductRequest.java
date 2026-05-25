package com.grocery.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull @Positive
    private BigDecimal price;
    private BigDecimal mrp;
    @PositiveOrZero
    private int stock;
    private String unit;
    @NotNull
    private Long categoryId;
    private List<String> imageUrls;
}
