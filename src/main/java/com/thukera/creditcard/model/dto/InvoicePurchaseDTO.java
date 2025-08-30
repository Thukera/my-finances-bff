package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
	private Integer totalInstallments;
	private Integer currentInstallment;
	private BigDecimal value;
	@JsonFormat(pattern = "dd/MM/yyyy - hh:mm")
	private LocalDateTime purchaseDateTime;

	
	public static InvoicePurchaseDTO fromEntity(CreditPurchase purchase) {
	    return new InvoicePurchaseDTO(
	    		purchase.getPurchaseId(),
	    		purchase.getDescricao(),
	    		purchase.getTotalInstallments(),
	    		1,
	    		purchase.getValue(),
	    		purchase.getPurchaseDateTime()
	    );
	}
}
