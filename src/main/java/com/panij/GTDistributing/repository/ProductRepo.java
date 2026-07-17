package com.panij.GTDistributing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.panij.GTDistributing.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Integer> {
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
    // Exclude products with empty or NULL UPCs
    @Query("SELECT p FROM Product p WHERE TRIM(p.upc)= :upc AND p.upc IS NOT NULL AND p.upc <> ''")
    Optional<Product> findByUpc(@Param("upc") String upc);
    @Query("SELECT p FROM Product p ORDER BY p.item_Ref ASC")
    List<Product> findAllSortedByItemRef();
}


