package com.thukera.creditcard.model.form;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thukera.creditcard.model.dto.InvoiceDTOFromCreditCard;
import com.thukera.creditcard.model.dto.CreditCardDTOFromUser;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.user.model.entities.User;

import lombok.Data;

@Data
public class CreditCardForm {
	
	private long cardId;
	private User user;
	private String bank;
	private String nickname;
	private String endNumbers;
	private int dueDate;
	private int billingPeriodStart;
	private int billingPeriodEnd;
	private BigDecimal usedLimit;
	private BigDecimal totalLimit;
	private BigDecimal estimateLimit;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate cadastro;
	
	private List<InvoiceDTOFromCreditCard> invoices;
	
	public CreditCardForm() {
		super();
	}

	public CreditCardForm(long cardId, String bank, String endNumbers, String nickname, int billingInvoiceStart, int billingInvoiceEnd,
			BigDecimal usedLimit, BigDecimal totalLimit, BigDecimal estimateLimit, LocalDate cadastro) {
		super();
		this.cardId = cardId;
		this.bank = bank;
		this.endNumbers = endNumbers;
		this.nickname = nickname;
		this.billingPeriodStart = billingInvoiceStart;
		this.billingPeriodEnd = billingInvoiceEnd;
		this.usedLimit = usedLimit;
		this.totalLimit = totalLimit;
		this.estimateLimit = estimateLimit;
		this.cadastro = cadastro;
	}
	
	public CreditCardForm(long cardId, String bank, String endNumbers, String nickname, int billingInvoiceStart, int billingInvoiceEnd,
			BigDecimal usedLimit, BigDecimal totalLimit, BigDecimal estimateLimit, LocalDate cadastro, List<InvoiceDTOFromCreditCard> invoices) {
		super();
		this.cardId = cardId;
		this.bank = bank;
		this.endNumbers = endNumbers;
		this.nickname = nickname;
		this.billingPeriodStart = billingInvoiceStart;
		this.billingPeriodEnd = billingInvoiceEnd;
		this.usedLimit = usedLimit;
		this.totalLimit = totalLimit;
		this.estimateLimit = estimateLimit;
		this.cadastro = cadastro;
		this.invoices = invoices;
	}
	
	
	public CreditCard toModel() {
		return new CreditCard(user,bank,endNumbers,nickname,billingPeriodStart,billingPeriodEnd,usedLimit,totalLimit,estimateLimit,cadastro);
	}
	
	public CreditCardForm fromModel(CreditCard creditcard) {
		return new CreditCardForm(creditcard.getCardId(), creditcard.getBank(), creditcard.getEndnumbers(), creditcard.getNickname(),creditcard.getBillingPeriodStart(), creditcard.getBillingPeriodEnd(), creditcard.getUsedLimit(), creditcard.getTotalLimit(), creditcard.getEstimateLimit(),creditcard.getDataCadastro(), 
				creditcard.getInvoices().stream()
                .map(InvoiceDTOFromCreditCard::fromEntity)
                .collect(Collectors.toList()));
	}

}
