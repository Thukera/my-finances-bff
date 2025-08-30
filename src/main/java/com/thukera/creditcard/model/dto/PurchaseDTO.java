package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thukera.creditcard.model.entities.CreditPurchase;
import com.thukera.creditcard.model.entities.PurchaseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDTO {
	
	private Long purchaseId;
	private String descricao;
	private Integer totalInstallments;
	private BigDecimal value;
	@JsonFormat(pattern = "dd/MM/yyyy - hh:mm")
	private LocalDateTime purchaseDateTime;
	private PurchaseCategory category;
	private PurchaseInvoiceDTO invoice;
	
	public static PurchaseDTO fromEntity(CreditPurchase purchase) {
        return new PurchaseDTO(
        	purchase.getPurchaseId(),
        	purchase.getDescricao(),
        	purchase.getTotalInstallments(),	
        	purchase.getValue(),
        	purchase.getPurchaseDateTime(),
        	purchase.getCategory(),
        	PurchaseInvoiceDTO.fromEntity(purchase.getInvoice())
        );
    }
}

