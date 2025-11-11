package com.freelancing.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class ServiceRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "customer_id")
	private User customer;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "service_id")
	private Service service;

	private String requirement_description;
	
	private String requestTime;

	private String requirement_filename;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "service_negotiation_id")
	private ServiceNegotiation approvedNegotiation;  // Approve negotiation mapping
	
	@OneToMany(mappedBy = "serviceRequest", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private List<ServiceNegotiation> serviceNegotiations = new ArrayList<>();
	
	@OneToMany(mappedBy = "serviceRequest", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private List<ServiceRequestMessage> messages = new ArrayList<>();

	private String status;

}
