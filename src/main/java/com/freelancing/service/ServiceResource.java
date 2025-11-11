package com.freelancing.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import com.freelancing.dao.CategoryDao;
import com.freelancing.dao.ServiceDao;
import com.freelancing.dao.ServiceNegotiationDao;
import com.freelancing.dao.ServiceRequestDao;
import com.freelancing.dao.ServiceRequestMessageDao;
import com.freelancing.dao.UserDao;
import com.freelancing.dto.AddNegotiationRequest;
import com.freelancing.dto.AddServiceRequest;
import com.freelancing.dto.CommonApiResponse;
import com.freelancing.dto.CustomerAddServiceRequest;
import com.freelancing.dto.ServiceRequestResponse;
import com.freelancing.dto.ServiceResponse;
import com.freelancing.entity.Category;
import com.freelancing.entity.Service;
import com.freelancing.entity.ServiceNegotiation;
import com.freelancing.entity.ServiceRequest;
import com.freelancing.entity.User;
import com.freelancing.exception.ServiceSaveFailedException;
import com.freelancing.utility.Constants.ActiveStatus;
import com.freelancing.utility.Constants.ServiceNegotiationStatus;
import com.freelancing.utility.Constants.ServiceRequestStatus;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Component
@Transactional
public class ServiceResource {

	private final Logger LOG = LoggerFactory.getLogger(ServiceResource.class);

	@Autowired
	private ServiceDao serviceDao;

	@Autowired
	private ServiceRequestDao serviceRequestDao;

	@Autowired
	private ServiceNegotiationDao serviceNegotiationDao;

	@Autowired
	private ServiceRequestMessageDao serviceRequestMessageDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private StorageService storageService;

	@Autowired
	private CategoryDao categoryDao;

	public ResponseEntity<CommonApiResponse> addService(AddServiceRequest request) {

		LOG.info("request received for Service add");

		CommonApiResponse response = new CommonApiResponse();

		if (request == null) {
			response.setResponseMessage("missing input");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (request.getName() == null || request.getDescription() == null || request.getMinPrice() == null
				|| request.getDeliveryTime() == 0 || request.getTechExpertId() == 0 || request.getImage1() == null
				|| request.getImage2() == null || request.getImage3() == null) {
			response.setResponseMessage("bad request - missing input!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		String addedDateTime = String
				.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

		Category subCategory = null;

		subCategory = this.categoryDao.findById(request.getSubCategoryId()).get();

		if (subCategory == null) {
			response.setResponseMessage("Category not found");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		User techExpert = this.userDao.findById(request.getTechExpertId()).get();

		// store service image in Image Folder and give event name to store in database
		String image1 = storageService.store(request.getImage1());
		String image2 = storageService.store(request.getImage2());
		String image3 = storageService.store(request.getImage3());

		Service service = AddServiceRequest.toServiceEntity(request);
		service.setImage1(image1);
		service.setImage2(image2);
		service.setImage3(image3);

		service.setSubCategory(subCategory);

		service.setAddedTime(addedDateTime);
		service.setStatus(ActiveStatus.ACTIVE.value());
		service.setTechExpert(techExpert);

		Service savedService = this.serviceDao.save(service);

		if (savedService == null) {
			throw new ServiceSaveFailedException("Failed to save the Service");
		}

		response.setResponseMessage("Service added successful");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);

	}

	public ResponseEntity<ServiceResponse> fetchAllServices() {

		LOG.info("Request received for fetching all services");

		ServiceResponse response = new ServiceResponse();

		List<Service> services = new ArrayList<>();

		services = this.serviceDao.findByStatus(ActiveStatus.ACTIVE.value());

		if (CollectionUtils.isEmpty(services)) {
			response.setResponseMessage("No Services found");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.OK);
		}

		response.setServices(services);
		response.setResponseMessage("Services fetched successful");
		response.setSuccess(true);

		return new ResponseEntity<ServiceResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<CommonApiResponse> deleteService(int serviceId) {

		LOG.info("Request received for deleting category");

		CommonApiResponse response = new CommonApiResponse();

		if (serviceId == 0) {
			response.setResponseMessage("missing service Id");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Service service = this.serviceDao.findById(serviceId).get();

		if (service == null) {
			response.setResponseMessage("service not found");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		service.setStatus(ActiveStatus.DEACTIVATED.value());
		Service updatedService = this.serviceDao.save(service);

		if (updatedService == null) {
			throw new ServiceSaveFailedException("Failed to delete the Service");
		}

		response.setResponseMessage("Service Deleted Successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);

	}

	public void fetchServiceImage(String serviceImageName, HttpServletResponse resp) {
		Resource resource = storageService.load(serviceImageName);
		if (resource != null) {
			try (InputStream in = resource.getInputStream()) {
				ServletOutputStream out = resp.getOutputStream();
				FileCopyUtils.copy(in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ResponseEntity<ServiceResponse> fetchAllServicesByTechExpert(Integer techExpertId) {

		LOG.info("Request received for fetching all services");

		ServiceResponse response = new ServiceResponse();

		if (techExpertId == 0) {
			response.setResponseMessage("missing tech expert Id");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User techExpert = this.userDao.findById(techExpertId).get();

		if (techExpert == null) {
			response.setResponseMessage("tech expert not found!!");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Service> services = new ArrayList<>();

		services = this.serviceDao.findByTechExpert(techExpert);

		if (CollectionUtils.isEmpty(services)) {
			response.setResponseMessage("No Services found");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.OK);
		}

		response.setServices(services);
		response.setResponseMessage("Services fetched successful");
		response.setSuccess(true);

		return new ResponseEntity<ServiceResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<ServiceResponse> fetchAllServicesBySubCategory(Integer subCategoryId) {

		LOG.info("Request received for fetching all services using sub category id");

		ServiceResponse response = new ServiceResponse();

		if (subCategoryId == 0) {
			response.setResponseMessage("missing category");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Category subCategory = this.categoryDao.findById(subCategoryId).get();

		List<Service> services = new ArrayList<>();

		services = this.serviceDao.findBySubCategoryAndStatus(subCategory, ActiveStatus.ACTIVE.value());

		if (CollectionUtils.isEmpty(services)) {
			response.setResponseMessage("No Services found");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.OK);
		}

		response.setServices(services);
		response.setResponseMessage("Services fetched successful");
		response.setSuccess(true);

		return new ResponseEntity<ServiceResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<CommonApiResponse> addServiceRequest(CustomerAddServiceRequest request) {

		LOG.info("request received for adding service request");

		CommonApiResponse response = new CommonApiResponse();

		if (request == null || request.getServiceId() == 0) {
			response.setResponseMessage("missing input");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (request.getCustomerId() == 0 || request.getRequirement_filename() == null
				|| request.getRequirement_filename() == null) {
			response.setResponseMessage("bad request - missing input!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		String addedDateTime = String
				.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

		User customer = this.userDao.findById(request.getCustomerId()).get();

		// store service image in Image Folder and give event name to store in database
		String image1 = storageService.store(request.getRequirement_filename());

		Service service = this.serviceDao.findById(request.getServiceId()).get();

		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setCustomer(customer);
		serviceRequest.setRequirement_description(request.getRequirement_description());
		serviceRequest.setService(service);
		serviceRequest.setStatus(ServiceRequestStatus.PENDING.value());
		serviceRequest.setRequestTime(addedDateTime);
		serviceRequest.setRequirement_filename(image1);

		ServiceRequest savedServiceRequest = this.serviceRequestDao.save(serviceRequest);

		if (savedServiceRequest == null) {
			throw new ServiceSaveFailedException("Failed to add the service request");
		}

		response.setResponseMessage("Service Request added successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);

	}

	public ResponseEntity<CommonApiResponse> closeServiceRequest(Integer serviceRequestId) {

		LOG.info("Request received for closing the service request");

		CommonApiResponse response = new CommonApiResponse();

		if (serviceRequestId == 0) {
			response.setResponseMessage("missing customer");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		ServiceRequest serviceRequest = this.serviceRequestDao.findById(serviceRequestId).get();
		serviceRequest.setStatus(ServiceRequestStatus.CLOSE.value());

		ServiceRequest updatedRequest = this.serviceRequestDao.save(serviceRequest);

		if (updatedRequest == null) {
			response.setResponseMessage("Failed to cancel Service Request!!!");
			response.setSuccess(true);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
		} else {
			response.setResponseMessage("Service Request Cancel Successful!!!");
			response.setSuccess(true);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
		}

	}

	public ResponseEntity<CommonApiResponse> updateServiceRequest(Integer serviceRequestId, String status) {

		LOG.info("Request received for updating the service request");

		CommonApiResponse response = new CommonApiResponse();

		if (serviceRequestId == 0) {
			response.setResponseMessage("missing customer");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		ServiceRequest serviceRequest = this.serviceRequestDao.findById(serviceRequestId).get();
		serviceRequest.setStatus(status);

		ServiceRequest updatedRequest = this.serviceRequestDao.save(serviceRequest);

		if (updatedRequest == null) {
			response.setResponseMessage("Failed to update Service Request!!!");
			response.setSuccess(true);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
		} else {
			response.setResponseMessage("Service Request Update Successful!!!");
			response.setSuccess(true);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
		}

	}

	public ResponseEntity<ServiceRequestResponse> fetchServiceRequestByCustomer(Integer customerId) {

		LOG.info("Request received for fetching all services request by customer");

		ServiceRequestResponse response = new ServiceRequestResponse();

		if (customerId == 0) {
			response.setResponseMessage("missing customer");
			response.setSuccess(false);

			return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User customer = this.userDao.findById(customerId).get();

		List<ServiceRequest> serviceRequests = new ArrayList<>();

		serviceRequests = this.serviceRequestDao.findByCustomer(customer);

		if (CollectionUtils.isEmpty(serviceRequests)) {
			response.setResponseMessage("No Service Requests found!!!");
			response.setSuccess(true);

			return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.OK);
		}

		response.setServiceRequests(serviceRequests);
		response.setResponseMessage("Service Request fetched successful");
		response.setSuccess(true);

		return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<ServiceRequestResponse> fetchAllServiceRequestByTechExpert(Integer techExpertId) {

		LOG.info("Request received for fetching all services request by customer");

		ServiceRequestResponse response = new ServiceRequestResponse();

		if (techExpertId == 0) {
			response.setResponseMessage("missing tech expert");
			response.setSuccess(false);

			return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User techExpert = this.userDao.findById(techExpertId).get();

		List<ServiceRequest> serviceRequests = new ArrayList<>();

		serviceRequests = this.serviceRequestDao.findByTechExpert(techExpert);

		if (CollectionUtils.isEmpty(serviceRequests)) {
			response.setResponseMessage("No Service Requests found!!!");
			response.setSuccess(true);

			return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.OK);
		}

		response.setServiceRequests(serviceRequests);
		response.setResponseMessage("Service Request fetched successful");
		response.setSuccess(true);

		return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<ServiceRequestResponse> fetchAllServiceRequestByService(Integer serviceId) {

		LOG.info("Request received for fetching the service request by using service");

		ServiceRequestResponse response = new ServiceRequestResponse();

		if (serviceId == 0) {
			response.setResponseMessage("missing tech expert");
			response.setSuccess(false);

			return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Service service = this.serviceDao.findById(serviceId).get();

		List<ServiceRequest> serviceRequests = new ArrayList<>();

		serviceRequests = this.serviceRequestDao.findByService(service);

		if (CollectionUtils.isEmpty(serviceRequests)) {
			response.setResponseMessage("No Service Requests found!!!");
			response.setSuccess(true);

			return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.OK);
		}

		response.setServiceRequests(serviceRequests);
		response.setResponseMessage("Service Request fetched successful");
		response.setSuccess(true);

		return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<ServiceRequestResponse> fetchAllServiceRequests() {

		LOG.info("Request received for fetching all services request by customer");

		ServiceRequestResponse response = new ServiceRequestResponse();

		List<ServiceRequest> serviceRequests = new ArrayList<>();

		serviceRequests = this.serviceRequestDao.findAll();

		if (CollectionUtils.isEmpty(serviceRequests)) {
			response.setResponseMessage("No Service Requests found!!!");
			response.setSuccess(true);

			return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.OK);
		}

		response.setServiceRequests(serviceRequests);
		response.setResponseMessage("Service Request fetched successful");
		response.setSuccess(true);

		return new ResponseEntity<ServiceRequestResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<CommonApiResponse> addServiceRequestNegotiation(AddNegotiationRequest request) {

		LOG.info("request received for adding service request");

		CommonApiResponse response = new CommonApiResponse();

		if (request == null || request.getServiceRequestId() == 0) {
			response.setResponseMessage("missing input");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (request.getEstimatedTime() == 0 || request.getPlan() == null || request.getPrice() == null) {
			response.setResponseMessage("bad request - missing input!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		String addedDateTime = String
				.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

		User user = this.userDao.findById(request.getUserId()).get();

		ServiceRequest serviceRequest = this.serviceRequestDao.findById(request.getServiceRequestId()).get();

		ServiceNegotiation negotiation = new ServiceNegotiation();
		negotiation.setCustomer(serviceRequest.getCustomer());
		negotiation.setDateTime(addedDateTime);
		negotiation.setEstimatedTime(request.getEstimatedTime());
		negotiation.setPlan(request.getPlan());
		negotiation.setPrice(request.getPrice());
		negotiation.setServiceRequest(serviceRequest);
		negotiation.setStatus(ServiceNegotiationStatus.PENDING.value());
		negotiation.setUser(user);

		ServiceNegotiation savedServiceNegotiation = this.serviceNegotiationDao.save(negotiation);

		if (savedServiceNegotiation == null) {
			throw new ServiceSaveFailedException("Failed to add the service request negotiation");
		}

		response.setResponseMessage("Service Plan added successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);

	}

	public ResponseEntity<CommonApiResponse> addServiceRequestNegotiationCustomer(AddNegotiationRequest request) {

		LOG.info("request received for adding service request");

		CommonApiResponse response = new CommonApiResponse();

		if (request == null || request.getNegotiationId() == 0) {
			response.setResponseMessage("missing input");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (request.getStatus() == null || request.getCustomerMessage() == null) {
			response.setResponseMessage("bad request - missing input!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		ServiceNegotiation negotiation = this.serviceNegotiationDao.findById(request.getNegotiationId()).get();

		ServiceRequest serviceRequest = negotiation.getServiceRequest();

		negotiation.setMessage(request.getCustomerMessage());

		if (request.getStatus().equals(ServiceNegotiationStatus.APPROVED.value())) {

			User customer = serviceRequest.getCustomer();

			if (customer.getWalletAmount().compareTo(negotiation.getPrice()) < 0) {
				response.setResponseMessage("Insufficient Fund in Wallet, Can't Approve the Tech Expert Plan!!!");
				response.setSuccess(false);

				return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
			}

			serviceRequest.setStatus(ServiceRequestStatus.APPROVED.value());
			serviceRequest.setApprovedNegotiation(negotiation);

			User techExpert = serviceRequest.getService().getTechExpert();

			customer.setWalletAmount(customer.getWalletAmount().subtract(negotiation.getPrice()));
			techExpert.setWalletAmount(techExpert.getWalletAmount().add(negotiation.getPrice()));

			this.userDao.save(customer);
			this.userDao.save(techExpert);
			this.serviceRequestDao.save(serviceRequest);
		} else if (request.getStatus().equals(ServiceNegotiationStatus.CLOSE.value())) {
			serviceRequest.setStatus(ServiceRequestStatus.CLOSE.value());
			this.serviceRequestDao.save(serviceRequest);
		}

		// in case of deny, no need of updating anything just set status of negotiation
		// as DENY

		negotiation.setStatus(request.getStatus());

		ServiceNegotiation savedServiceNegotiation = this.serviceNegotiationDao.save(negotiation);

		if (savedServiceNegotiation == null) {
			throw new ServiceSaveFailedException("Failed to update the service request negotiation");
		}

		response.setResponseMessage("Service Negotiation updated successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);

	}

	public ResponseEntity<ServiceResponse> fetchServiceById(Integer serviceId) {

		LOG.info("Request received for fetching all services");

		ServiceResponse response = new ServiceResponse();

		if (serviceId == 0) {
			response.setResponseMessage("missing service Id");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Service service = this.serviceDao.findById(serviceId).get();

		if (service == null) {
			response.setResponseMessage("service not found!!");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.BAD_REQUEST);
		}

		service = this.serviceDao.findById(serviceId).get();

		if (service == null) {
			response.setResponseMessage("Service not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<ServiceResponse>(response, HttpStatus.OK);
		}

		response.setServices(Arrays.asList(service));
		response.setResponseMessage("Services fetched successful");
		response.setSuccess(true);

		return new ResponseEntity<ServiceResponse>(response, HttpStatus.OK);

	}

	public ResponseEntity<Resource> downloadRequirment(String requirementFileName, HttpServletResponse response) {

		Resource resource = storageService.load(requirementFileName);
		if (resource == null) {
			// Handle file not found
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Customer_Requirement\"")
				.body(resource);

	}

}
