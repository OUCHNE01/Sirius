package com.freelancing.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.freelancing.entity.Service;
import com.freelancing.entity.ServiceRequest;
import com.freelancing.entity.User;

@Repository
public interface ServiceRequestDao extends JpaRepository<ServiceRequest, Integer> {

	List<ServiceRequest> findByCustomer(User customer);
	
	List<ServiceRequest> findByService(Service service);

	@Query("SELECT r FROM ServiceRequest r WHERE r.service.techExpert = :techExpert")
	List<ServiceRequest> findByTechExpert(User techExpert);

}
