package com.excelr.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.excelr.model.Laptops;
@Repository
public interface LaptopsRepo extends JpaRepository<Laptops, Long> {

}
