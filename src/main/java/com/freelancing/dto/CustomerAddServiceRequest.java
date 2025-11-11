package com.freelancing.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CustomerAddServiceRequest {

	private Integer customerId;

	private Integer serviceId;

	private String requirement_description;

	private MultipartFile requirement_filename;

}
