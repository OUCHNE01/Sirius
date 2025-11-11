package com.freelancing.dto;

import java.util.ArrayList;
import java.util.List;

import com.freelancing.entity.ServiceRequest;

import lombok.Data;

@Data
public class ServiceRequestResponse extends CommonApiResponse {
	
	private List<ServiceRequest> serviceRequests = new ArrayList<>();

}
