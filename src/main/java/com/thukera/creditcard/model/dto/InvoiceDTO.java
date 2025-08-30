package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.enums.InvoiceStatus;
import com.thukera.user.dto.UserDTO;
import com.thukera.user.model.entities.User;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTO {

	private Long invoiceId;
	private InvoiceCreditCardDTO creditCardDto;
	private BigDecimal totalAmount;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate dueDate;
	private InvoiceStatus status;
//	private List<CreditPurchase> purchases;
	
	public static InvoiceDTO fromEntity(Invoice invoice) {
	    return new InvoiceDTO(
	        invoice.getInvoiceId(),
	        InvoiceCreditCardDTO.fromEntity(invoice.getCreditCard()), // no stream
	        invoice.getTotalAmount(),
	        invoice.getStartDate(),
	        invoice.getEndDate(),
	        invoice.getDueDate(),
	        invoice.getStatus()
	    );
	}
}
