package com.thukera.creditcard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thukera.creditcard.model.entities.PurchaseCategory;

@Repository
public interface PurchaseCategoryRepository extends JpaRepository<PurchaseCategory, Long>{
	
	boolean existsByName(String name);
	PurchaseCategory getByName(String category);

}
