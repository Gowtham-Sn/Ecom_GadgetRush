package com.excelr.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.excelr.model.AllProducts;
@Repository
public interface AllProductsRepo extends JpaRepository<AllProducts, Long> {
	@Query("SELECT p FROM AllProducts p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<AllProducts> searchProducts(String query);
}