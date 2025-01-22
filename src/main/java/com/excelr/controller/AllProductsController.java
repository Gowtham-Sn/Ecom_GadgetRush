package com.excelr.controller;

import com.excelr.model.AllProducts;
import com.excelr.service.AllProductsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AllProductsController {

    private final AllProductsService allProductsService;

    public AllProductsController(AllProductsService allProductsService) {
        this.allProductsService = allProductsService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String query) {
        List<AllProducts> products = allProductsService.searchProducts(query);
        if (products.isEmpty()) {
            return ResponseEntity.ok("No results found");
        }
        return ResponseEntity.ok(products);
    }
}