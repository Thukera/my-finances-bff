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
public class InvoiceDTOFromCreditCard {
    private Long id;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal estimateLimit;
    private InvoiceStatus status;

    public static InvoiceDTOFromCreditCard fromEntity(Invoice invoice) {
        return new InvoiceDTOFromCreditCard(
            invoice.getInvoiceId(),
            invoice.getDueDate(),
            invoice.getTotalAmount(),
            invoice.getEstimateLimit(),
            invoice.getStatus()
        );
    }
}

