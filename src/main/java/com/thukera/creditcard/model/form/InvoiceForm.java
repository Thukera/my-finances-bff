package com.thukera.creditcard.model.form;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class InvoiceForm {

	private BigDecimal estimateLimit;
	
	public InvoiceForm() {
		super();
	}
	
}
