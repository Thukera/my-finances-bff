package com.thukera.creditcard.model.form;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.user.model.entities.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreditCardForm {
	
	private long cardId;
	private User user;
	private String bank;
	private String endNumbers;
	private int billingPeriodStart;
	private int billingPeriodEnd;
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
		this.billingPeriodStart = billingInvoiceStart;
		this.billingPeriodEnd = billingInvoiceEnd;
		this.usedLimit = usedLimit;
		this.totalLimit = totalLimit;
		this.cadastro = cadastro;
	}
	
	
	public CreditCard toModel() {
		return new CreditCard(user,bank,endNumbers,billingPeriodStart,billingPeriodEnd,usedLimit,totalLimit,cadastro);
	}
	
	public CreditCardForm fromModel(CreditCard creditcard) {
		return new CreditCardForm(creditcard.getCardId(), creditcard.getBank(), creditcard.getEndnumbers(), creditcard.getBillingPeriodStart(), creditcard.getBillingPeriodEnd(), creditcard.getUsedLimit(), creditcard.getTotalLimit(), creditcard.getDataCadastro());
	}

}
