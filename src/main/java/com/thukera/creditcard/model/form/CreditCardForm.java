package com.thukera.creditcard.model.form;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.user.model.entities.User;

import lombok.Data;

@Data
public class CreditCardForm {
	
	private long cardId;
	private User user;
	private String bank;
	private String endNumbers;
	private int billingInvoiceStart;
	private int billingInvoiceEnd;
	private BigDecimal usedLimit;
	private BigDecimal totalLimit;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate cadastro;
	
	public CreditCardForm() {
		super();
	}

	public CreditCardForm(long cardId, String bank, String endNumbers, int billingInvoiceStart, int billingInvoiceEnd,
			BigDecimal usedLimit, BigDecimal totalLimit, LocalDate cadastro) {
		super();
		this.cardId = cardId;
		this.bank = bank;
		this.endNumbers = endNumbers;
		this.billingInvoiceStart = billingInvoiceStart;
		this.billingInvoiceEnd = billingInvoiceEnd;
		this.usedLimit = usedLimit;
		this.totalLimit = totalLimit;
		this.cadastro = cadastro;
	}
	
	
	public CreditCard toModel() {
		return new CreditCard(user,bank,endNumbers,billingInvoiceStart,billingInvoiceEnd,usedLimit,totalLimit,cadastro);
	}
	
	public CreditCardForm fromModel(CreditCard creditcard) {
		return new CreditCardForm(creditcard.getCardId(), creditcard.getBank(), creditcard.getEndnumbers(), creditcard.getBillingPeriodStart(), creditcard.getBillingPeriodEnd(), creditcard.getUsedLimit(), creditcard.getTotalLimit(), creditcard.getDataCadastro());
	}

}
