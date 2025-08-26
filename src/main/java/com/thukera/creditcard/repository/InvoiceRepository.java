package com.thukera.creditcard.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.enums.InvoiceStatus;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
	
	Optional<Invoice> findByCreditCardAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
	        CreditCard creditCard,
	        InvoiceStatus status,
	        LocalDate startDate,
	        LocalDate endDate);

    Optional<Invoice> findByCreditCardAndStatus(CreditCard creditCard, InvoiceStatus status);

}
