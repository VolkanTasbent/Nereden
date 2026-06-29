package com.nereden.api.domain.repository;

import com.nereden.api.domain.entity.Product;
import com.nereden.api.domain.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByAnalysisRequestId(UUID analysisRequestId);

    Page<Product> findByCategory(ProductCategory category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(COALESCE(p.brand, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR p.category = :category) AND (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(COALESCE(p.brand, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchByCategory(
            @Param("query") String query,
            @Param("category") ProductCategory category,
            Pageable pageable
    );
}
