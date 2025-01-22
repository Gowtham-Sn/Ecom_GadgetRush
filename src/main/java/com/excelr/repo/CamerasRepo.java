package com.excelr.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.excelr.model.Cameras;
@Repository
public interface CamerasRepo extends JpaRepository<Cameras, Long> {

}
