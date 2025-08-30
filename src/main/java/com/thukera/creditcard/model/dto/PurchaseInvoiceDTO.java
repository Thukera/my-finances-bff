package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.entities.PurchaseCategory;
import com.thukera.creditcard.model.enums.InvoiceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseInvoiceDTO {
	
	private Long invoiceId;
	private InvoiceCreditCardDTO creditcard;
	private LocalDate dueDate;
	private InvoiceStatus status;
//	private List<CreditPurchase> purchases;
	
	public static PurchaseInvoiceDTO fromEntity(Invoice invoice) {
	    return new PurchaseInvoiceDTO(
	        invoice.getInvoiceId(),
	        InvoiceCreditCardDTO.fromEntity(invoice.getCreditCard()),
	        invoice.getDueDate(),
	        invoice.getStatus()
	    );
	}

}
