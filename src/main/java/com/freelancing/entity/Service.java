package com.freelancing.entity;

import java.math.BigDecimal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Service {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String name;

	private String description;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "category_id")
	private Category subCategory;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tech_expert_id")
	private User techExpert;
	
	private String addedTime;

	private BigDecimal minPrice;

	private Integer deliveryTime; // in days

	private String image1;

	private String image2;

	private String image3;

	private String status;

}
