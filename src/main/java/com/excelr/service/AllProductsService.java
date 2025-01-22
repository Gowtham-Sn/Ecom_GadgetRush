package com.excelr.service;

import com.excelr.model.AllProducts;
import com.excelr.repo.AllProductsRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AllProductsService {

    private final AllProductsRepo allProductsRepository;

    public AllProductsService(AllProductsRepo allProductsRepo) {
        this.allProductsRepository = allProductsRepo;
    }

    public List<AllProducts> searchProducts(String query) {
        return allProductsRepository.searchProducts(query);
    }
}