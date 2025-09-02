package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thukera.creditcard.model.entities.CreditPurchase;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDTOFromInvoice {

	private Long purchaseId;
	private String descricao;
	private BigDecimal value;
	@JsonFormat(pattern = "dd/MM/yyyy - hh:mm")
	private LocalDateTime purchaseDateTime;
	private InstallmentDTOFromInvoice installment;

	public static PurchaseDTOFromInvoice fromEntity(CreditPurchase purchase, Invoice invoice) {
	    InstallmentDTOFromInvoice currentInstallment = purchase.getInstallments().stream()
	        .filter(inst -> inst.getInvoice() != null &&
	                        inst.getInvoice().getInvoiceId().equals(invoice.getInvoiceId()))
	        .findFirst()
	        .map(InstallmentDTOFromInvoice::fromEntity)
	        .orElse(null);

	    return new PurchaseDTOFromInvoice(
	        purchase.getPurchaseId(),
	        purchase.getDescricao(),
	        purchase.getValue(),
	        purchase.getPurchaseDateTime(),
	        currentInstallment
	    );
	}
}
