package com.thukera.creditcard.model.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class CreditPurchaseForm {
	
	private long puchaseID;
	private long creditCardId;
	private long invoiceId;
	private int totalInstallments;
	private String category;
	private BigDecimal value;
	
	@JsonFormat(pattern = "dd/MM/yyyy - hh:mm")
	private LocalDateTime purchaseDateTime;

	
	

}
