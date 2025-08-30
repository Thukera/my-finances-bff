package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.enums.InvoiceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardInvoiceDTO {
    private Long id;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;

    public static CreditCardInvoiceDTO fromEntity(Invoice invoice) {
        return new CreditCardInvoiceDTO(
            invoice.getInvoiceId(),
            invoice.getDueDate(),
            invoice.getTotalAmount(),
            invoice.getStatus()
        );
    }
}

