package com.grocery.controller;

import com.grocery.dto.CategoryRequest;
import com.grocery.entity.Category;
import com.grocery.repository.CategoryRepository;
import com.grocery.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository categories;
    private final ProductService productService;

    @GetMapping
    public List<Category> list() {
        return categories.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Category create(@Valid @RequestBody CategoryRequest request) {
        return productService.createCategory(request);
    }
}
