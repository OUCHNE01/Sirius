package com.freelancing.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AddNegotiationRequest {

	// for customer
	private Integer negotiationId;
	
	private String status;
	
	private String customerMessage;
	
	private int userId;   // common for both
	
	// for tech expert
	private int serviceRequestId;
	
	private String plan;
	
	private BigDecimal price;
	
	private Integer estimatedTime;
	
}
