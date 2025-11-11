package com.freelancing.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.freelancing.dto.AddNegotiationRequest;
import com.freelancing.dto.AddServiceRequest;
import com.freelancing.dto.CommonApiResponse;
import com.freelancing.dto.CustomerAddServiceRequest;
import com.freelancing.dto.ServiceRequestResponse;
import com.freelancing.dto.ServiceResponse;
import com.freelancing.service.ServiceResource;
import com.lowagie.text.DocumentException;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api/service")
@CrossOrigin(origins = "http://localhost:3000")
public class ServiceController {

	@Autowired
	private ServiceResource serviceResource;

	@PostMapping("/add")
	@Operation(summary = "Api to add service")
	public ResponseEntity<CommonApiResponse> addService(AddServiceRequest request) {
		return serviceResource.addService(request);
	}

	@GetMapping("/fetch/all")
	@Operation(summary = "Api to fetch all services")
	public ResponseEntity<ServiceResponse> fetchAllServices() {
		return serviceResource.fetchAllServices();
	}

	@GetMapping("/fetch/id-wise")
	@Operation(summary = "Api to fetch service by id")
	public ResponseEntity<ServiceResponse> fetchServiceById(@RequestParam("serviceId") Integer serviceId) {
		return serviceResource.fetchServiceById(serviceId);
	}

	@GetMapping("/fetch/tech-expert-wise")
	@Operation(summary = "Api to fetch all services by tech expert")
	public ResponseEntity<ServiceResponse> fetchAllServicesByTechExpert(
			@RequestParam("techExpertId") Integer techExpertId) {
		return serviceResource.fetchAllServicesByTechExpert(techExpertId);
	}

	@GetMapping("/fetch/sub-category-wise")
	@Operation(summary = "Api to fetch all services by category")
	public ResponseEntity<ServiceResponse> fetchAllServicesByCategory(
			@RequestParam("subCategoryId") Integer subCategoryId) {
		return serviceResource.fetchAllServicesBySubCategory(subCategoryId);
	}

	@DeleteMapping("/delete")
	@Operation(summary = "Api to delete the service")
	public ResponseEntity<CommonApiResponse> deleteService(@RequestParam("serviceId") int serviceId) {
		return serviceResource.deleteService(serviceId);
	}

	@GetMapping(value = "/{serviceImageName}", produces = "image/*")
	public void fetchTourImage(@PathVariable("serviceImageName") String serviceImageName, HttpServletResponse resp) {
		this.serviceResource.fetchServiceImage(serviceImageName, resp);
	}

	@PostMapping("/request/add")
	@Operation(summary = "Api to add service request")
	public ResponseEntity<CommonApiResponse> addServiceRequest(CustomerAddServiceRequest request) {
		return serviceResource.addServiceRequest(request);
	}

	// by customer and tech expert
	@GetMapping("/request/close")
	@Operation(summary = "Api to add service request")
	public ResponseEntity<CommonApiResponse> closeServiceRequest(
			@RequestParam("serviceRequestId") Integer serviceRequestId) {
		return serviceResource.closeServiceRequest(serviceRequestId);
	}

	@GetMapping("/request/update")
	@Operation(summary = "Api to add service request")
	public ResponseEntity<CommonApiResponse> updateServiceRequest(
			@RequestParam("serviceRequestId") Integer serviceRequestId, @RequestParam("status") String status) {
		return serviceResource.updateServiceRequest(serviceRequestId, status);
	}

	@GetMapping("/request/fetch/customer-wise")
	@Operation(summary = "Api to fetch service request by customer wise")
	public ResponseEntity<ServiceRequestResponse> fetchAllServiceRequestByCustomer(
			@RequestParam("customerId") Integer customerId) {
		return serviceResource.fetchServiceRequestByCustomer(customerId);
	}

	@GetMapping("/request/fetch/tech-expert-wise")
	@Operation(summary = "Api to fetch service request by tech expert wise")
	public ResponseEntity<ServiceRequestResponse> fetchAllServiceRequestByTechExpert(
			@RequestParam("techExpertId") Integer techExpertId) {
		return serviceResource.fetchAllServiceRequestByTechExpert(techExpertId);
	}

	@GetMapping("/request/fetch/service-wise")
	@Operation(summary = "Api to fetch service requests by service")
	public ResponseEntity<ServiceRequestResponse> fetchAllServiceRequestByService(
			@RequestParam("serviceId") Integer serviceId) {
		return serviceResource.fetchAllServiceRequestByService(serviceId);
	}

	@GetMapping("/request/fetch/all")
	@Operation(summary = "Api to fetch all service requests")
	public ResponseEntity<ServiceRequestResponse> fetchAllServiceRequests() {
		return serviceResource.fetchAllServiceRequests();
	}

	// by tech expert
	@PostMapping("/request/negotiation/add")
	@Operation(summary = "Api to add service request negotiation ")
	public ResponseEntity<CommonApiResponse> addServiceRequestNegotiation(@RequestBody AddNegotiationRequest request) {
		return serviceResource.addServiceRequestNegotiation(request);
	}

	// by customer
	@PostMapping("/request/negotiation/customer/update")
	@Operation(summary = "Api to update customer negotiation")
	public ResponseEntity<CommonApiResponse> addServiceRequestNegotiationCustomer(
			@RequestBody AddNegotiationRequest request) {
		return serviceResource.addServiceRequestNegotiationCustomer(request);
	}

	// by tech expert
	@PostMapping("/request/negotiation/tech-expert/close")
	@Operation(summary = "Api to close service request from tech expert side")
	public ResponseEntity<CommonApiResponse> closeServiceRequestNegotiation(
			@RequestBody AddNegotiationRequest request) {
		return serviceResource.addServiceRequestNegotiationCustomer(request);
	}

	@GetMapping("request/requirement/{requirmentFileName}/download")
	@Operation(summary = "Api for downloading the service request customer requirement file")
	public ResponseEntity<Resource> downloadResume(@PathVariable("requirmentFileName") String resumeFileName,
			HttpServletResponse response) throws DocumentException, IOException {
		return this.serviceResource.downloadRequirment(resumeFileName, response);
	}

}
