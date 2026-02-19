package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thukera.creditcard.model.entities.Installment;
import com.thukera.creditcard.model.entities.Invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTOFromPurchase {
	
	private Long invoiceId;
	private String status;
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate dueDate;
	
	
	public static InvoiceDTOFromPurchase fromEntity(Invoice invoice) {
		return new InvoiceDTOFromPurchase(
				invoice.getInvoiceId(), 
				invoice.getStatus() != null ? invoice.getStatus().name() : null,
				invoice.getDueDate()
			);
	}

}
