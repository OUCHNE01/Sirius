package com.freelancing.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freelancing.entity.Category;
import com.freelancing.entity.Service;
import com.freelancing.entity.User;

@Repository
public interface ServiceDao extends JpaRepository<Service, Integer> {

	List<Service> findBySubCategoryAndStatus(Category category, String status);

	List<Service> findByStatus(String status);

	List<Service> findByTechExpert(User techExpert);
	
}
