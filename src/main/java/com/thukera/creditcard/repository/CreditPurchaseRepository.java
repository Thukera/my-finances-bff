package com.thukera.creditcard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.CreditPurchase;

@Repository
public interface CreditPurchaseRepository extends JpaRepository<CreditPurchase, Long>{
	
    @Query("""
            SELECT cp FROM CreditPurchase cp
            JOIN cp.category c
            JOIN cp.invoices i
            WHERE cp.creditCard = :creditCard
              AND c.repeat = true
              AND i.invoiceId = (
                  SELECT MAX(inv.invoiceId) 
                  FROM Invoice inv
                  WHERE inv.creditCard = :creditCard
              )
            """)
     List<CreditPurchase> findRepeatsOnLastInvoice(@Param("creditCard") CreditCard creditCard);

     @Query("""
            SELECT CASE WHEN COUNT(cp) > 0 THEN true ELSE false END
            FROM CreditPurchase cp
            JOIN cp.category c
            JOIN cp.invoices i
            WHERE cp.creditCard = :creditCard
              AND c.repeat = true
              AND i.invoiceId = (
                  SELECT MAX(inv.invoiceId) 
                  FROM Invoice inv
                  WHERE inv.creditCard = :creditCard
              )
            """)
     boolean existsRepeatsOnLastInvoice(@Param("creditCard") CreditCard creditCard);

}
