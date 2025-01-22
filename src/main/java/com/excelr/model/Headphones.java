package com.excelr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class Headphones {
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     private String name;
     private int cost;
     private int quantity;
     @Column(length = 1000)
     private String description;
     private String image;
}
