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
public class InvoicePurchaseDTO {

	private Long purchaseId;
	private String descricao;
	private BigDecimal value;
	@JsonFormat(pattern = "dd/MM/yyyy - hh:mm")
	private LocalDateTime purchaseDateTime;
	private List<PurchaseInstallmentDTO> installments;

	
	public static InvoicePurchaseDTO fromEntity(CreditPurchase purchase) {
	    return new InvoicePurchaseDTO(
	    	purchase.getPurchaseId(),
	    	purchase.getDescricao(),
	    	purchase.getValue(),
	    	purchase.getPurchaseDateTime(),
	    	purchase.getInstallments().stream().map(PurchaseInstallmentDTO::fromEntity).collect(Collectors.toList())
	    );
	}
}
