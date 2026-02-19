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
public class PurchaseDTO {
	
	private Long purchaseId;
	private String descricao;
	private boolean hasIinstallment;
	private BigDecimal value;
	//@JsonFormat(pattern = "dd-MM-yyyy hh:mm")
	private LocalDateTime purchaseDateTime;
	private PurchaseCategory category;
	private InvoiceDTOFromPurchase invoice;
	private BigDecimal installmentPayd;
	private List<InstallmentDTOFromPurchase> installments;
	
	public static PurchaseDTO fromEntity(CreditPurchase purchase) {
		
		InvoiceDTOFromPurchase currentInvoice = null;

	    // Only get invoice if purchase has no installments
	    if (!purchase.isHasInstallments() && !purchase.getInvoices().isEmpty()) {
	        currentInvoice = InvoiceDTOFromPurchase.fromEntity(
	            purchase.getInvoices().stream()
	                    .max((i1, i2) -> i1.getEndDate().compareTo(i2.getEndDate()))
	                    .orElse(null)
	        );
	    }
	    
        return new PurchaseDTO(
        	purchase.getPurchaseId(),
        	purchase.getDescricao(),
        	purchase.isHasInstallments(),	
        	purchase.getValue(),
        	purchase.getPurchaseDateTime(),
        	purchase.getCategory(),
        	currentInvoice,
        	purchase.getInstallments().stream().map(InstallmentDTOFromPurchase::fromEntity).collect(Collectors.toList()));
    }

	public PurchaseDTO() {
		super();
	}
	
	public PurchaseDTO(Long purchaseId, String descricao, boolean hasIinstallment, BigDecimal value,
			LocalDateTime purchaseDateTime, PurchaseCategory category, InvoiceDTOFromPurchase invoice) {
		super();
		this.purchaseId = purchaseId;
		this.descricao = descricao;
		this.hasIinstallment = hasIinstallment;
		this.value = value;
		this.purchaseDateTime = purchaseDateTime;
		this.category = category;
		this.invoice = invoice;
	}
	
	public PurchaseDTO(Long purchaseId, String descricao, boolean hasIinstallment, BigDecimal value,
			LocalDateTime purchaseDateTime, PurchaseCategory category, InvoiceDTOFromPurchase invoice,
			List<InstallmentDTOFromPurchase> installments) {
		super();
		this.purchaseId = purchaseId;
		this.descricao = descricao;
		this.hasIinstallment = hasIinstallment;
		this.value = value;
		this.purchaseDateTime = purchaseDateTime;
		this.category = category;
		this.invoice = invoice;
		this.installments = installments;
	}
	
}

