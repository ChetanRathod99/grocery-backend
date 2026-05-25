package com.grocery.repository;

import com.grocery.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndCategoryNameContainingIgnoreCase(String name, String category, Pageable pageable);
    Page<Product> findByActiveTrue(Pageable pageable);
    List<Product> findByStockLessThanEqualAndActiveTrue(int stock);
}
