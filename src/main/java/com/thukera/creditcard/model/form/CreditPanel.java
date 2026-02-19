package com.thukera.creditcard.model.form;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class CreditPanel {
	
	private BigDecimal usedLimit;
	private BigDecimal totalLimit;
	
	private BigDecimal totalInstallments;
	private BigDecimal paydInstallments;
	
	private List<CategoryPanel> categoryPanel;

}
