package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
	private boolean hasIinstallment;
	private BigDecimal value;
	@JsonFormat(pattern = "dd/MM/yyyy - hh:mm")
	private LocalDateTime purchaseDateTime;
	private PurchaseCategory category;
	private List<PurchaseInvoiceDTO> invoices;
	
	public static PurchaseDTO fromEntity(CreditPurchase purchase) {
        return new PurchaseDTO(
        	purchase.getPurchaseId(),
        	purchase.getDescricao(),
        	purchase.isHasInstallments(),	
        	purchase.getValue(),
        	purchase.getPurchaseDateTime(),
        	purchase.getCategory(),
        	purchase.getInvoices().stream().map(PurchaseInvoiceDTO::fromEntity).collect(Collectors.toList()));
    }
}

