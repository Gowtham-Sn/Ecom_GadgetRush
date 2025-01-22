package com.excelr.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.excelr.model.Headphones;
@Repository
public interface HeadphonesRepo extends JpaRepository<Headphones, Long> {

}
