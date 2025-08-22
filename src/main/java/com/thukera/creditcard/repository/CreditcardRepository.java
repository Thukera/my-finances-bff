package com.thukera.creditcard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thukera.creditcard.model.entities.CreditCard;

public interface CreditcardRepository extends JpaRepository<CreditCard, Long> {
	
	

}
