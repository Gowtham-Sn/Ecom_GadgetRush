package com.excelr.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.excelr.model.Watches;
@Repository
public interface WatchesRepo extends JpaRepository<Watches, Long> {

}
