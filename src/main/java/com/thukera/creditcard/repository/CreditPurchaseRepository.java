package com.thukera.creditcard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thukera.creditcard.model.entities.CreditPurchase;

@Repository
public interface CreditPurchaseRepository extends JpaRepository<CreditPurchase, Long>{

}
