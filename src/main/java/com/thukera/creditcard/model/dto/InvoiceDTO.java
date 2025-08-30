package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTO {

	private Long invoiceId;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate startDate;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate endDate;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dueDate;
	private InvoiceStatus status;
	private BigDecimal totalAmount;
	private InvoiceCreditCardDTO creditcard;
	private List<InvoicePurchaseDTO> purchases;
	
	public static InvoiceDTO fromEntity(Invoice invoice) {
	    return new InvoiceDTO(
	        invoice.getInvoiceId(),
	        invoice.getStartDate(),
	        invoice.getEndDate(),
	        invoice.getDueDate(),
	        invoice.getStatus(),
	        invoice.getTotalAmount(),
	        InvoiceCreditCardDTO.fromEntity(invoice.getCreditCard()),
	        invoice.getPurchases().stream()
            .map(InvoicePurchaseDTO::fromEntity)
            .collect(Collectors.toList())
	    );
	}
}
