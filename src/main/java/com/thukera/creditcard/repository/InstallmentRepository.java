package com.thukera.creditcard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thukera.creditcard.model.entities.Installment;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long>{

}
