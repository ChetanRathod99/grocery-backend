package com.grocery.service;

import com.grocery.dto.*;
import com.grocery.entity.*;
import com.grocery.exception.ResourceNotFoundException;
import com.grocery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;

    public Page<Product> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return products.findByActiveTrue(pageable);
        return products.findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndCategoryNameContainingIgnoreCase(q, q, pageable);
    }

    public Product get(Long id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public Category createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categories.save(category);
    }

    public Product create(ProductRequest request) {
        return products.save(apply(new Product(), request));
    }

    public Product update(Long id, ProductRequest request) {
        return products.save(apply(get(id), request));
    }

    public void delete(Long id) {
        Product product = get(id);
        product.setActive(false);
        products.save(product);
    }

    private Product apply(Product product, ProductRequest request) {
        Category category = categories.findById(request.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setMrp(request.getMrp());
        product.setStock(request.getStock());
        product.setUnit(request.getUnit());
        product.setCategory(category);
        product.setActive(true);
        product.getImageUrls().clear();
        if (request.getImageUrls() != null) product.getImageUrls().addAll(request.getImageUrls());
        return product;
    }
}
