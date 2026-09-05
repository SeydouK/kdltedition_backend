package com.kdlt.platform.product.repository;

import com.kdlt.platform.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    boolean existBySlug(String slug);
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findByActiveTrue();
}
