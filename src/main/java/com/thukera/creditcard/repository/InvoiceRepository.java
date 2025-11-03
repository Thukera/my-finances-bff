package com.thukera.creditcard.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.enums.InvoiceStatus;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	Optional<Invoice> findByCreditCardAndStatus(CreditCard creditCard, InvoiceStatus status);

	Optional<Invoice> findFirstByCreditCardAndStatusOrderByStartDateAsc(CreditCard creditCard, InvoiceStatus pending);

	Optional<Invoice> findByCreditCardAndStartDateAndEndDate(CreditCard card, LocalDate startDate, LocalDate endDate);

	@Query(value = """
			SELECT i.*
			FROM tb_invoice i
			WHERE i.card_id = :creditCardId
			  AND TO_DATE(:targetDate, 'YYYY-MM-DD') BETWEEN i.start_date AND i.end_date
			LIMIT 1
			""", nativeQuery = true)
	Optional<Invoice> findTargetInvoice(@Param("creditCardId") Long creditCardId,
			@Param("targetDate") LocalDate targetDate);

	@Query(value = """
			    SELECT i.invoice_id
			    FROM tb_invoice i
			    WHERE i.card_id = :creditCardId
			      AND TO_DATE(:targetDate, 'YYYY-MM-DD') BETWEEN i.start_date AND i.end_date
			    LIMIT 1
			""", nativeQuery = true)
	Optional<Long> findTargetInvoiceId(@Param("creditCardId") Long creditCardId,
			@Param("targetDate") LocalDate targetDate);

}
