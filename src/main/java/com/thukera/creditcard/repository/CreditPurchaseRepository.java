package com.thukera.creditcard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.CreditPurchase;

@Repository
public interface CreditPurchaseRepository extends JpaRepository<CreditPurchase, Long> {

	@Query(value = """
			SELECT cp.*
			FROM tb_credit_purchase cp
			JOIN tb_purchase_category pc
			    ON cp.tb_purchase_category = pc.purchase_category_id
			JOIN tb_invoice_purchase ip
			    ON cp.purchase_id = ip.purchase_id
			JOIN tb_invoice i
			    ON ip.invoice_id = i.invoice_id
			WHERE pc.repeat = true
			  AND i.card_id = :creditCardId
			  AND i.invoice_id = (
			        SELECT MAX(i2.invoice_id)
			        FROM tb_invoice i2
			        WHERE i2.card_id = :creditCardId
			  )
			""", nativeQuery = true)
	List<CreditPurchase> findRepeatPurchasesFromLastInvoice(@Param("creditCardId") Long creditCardId);

	@Query(value = """
			SELECT CASE WHEN COUNT(cp.purchase_id) > 0 THEN true ELSE false END
			FROM tb_credit_purchase cp
			JOIN tb_purchase_category pc
			    ON cp.tb_purchase_category = pc.purchase_category_id
			JOIN tb_invoice_purchase ip
			    ON cp.purchase_id = ip.purchase_id
			JOIN tb_invoice i
			    ON ip.invoice_id = i.invoice_id
			WHERE cp.card_id = :creditCardId
			  AND pc.repeat = true
			  AND i.invoice_id = (
			        SELECT MAX(i2.invoice_id)
			        FROM tb_invoice i2
			        WHERE i2.card_id = :creditCardId
			  )
			""", nativeQuery = true)
	boolean existsRepeatsOnLastInvoice(@Param("creditCardId") Long creditCardId);

}
