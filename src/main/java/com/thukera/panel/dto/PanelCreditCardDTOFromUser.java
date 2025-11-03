package com.thukera.panel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import com.thukera.creditcard.model.entities.CreditCard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PanelCreditCardDTOFromUser {
	private Long id;
	private String nickname;
	private String bank;
	private String endnumbers;
	private Integer billingPeriodStart;
	private Integer billingPeriodEnd;
	private BigDecimal totalLimit;
	private LocalDate dataCadastro;
	private Optional<Long> currentInvoice;

	public static PanelCreditCardDTOFromUser fromEntity(CreditCard card) {
		return new PanelCreditCardDTOFromUser(
				card.getCardId(), 
				card.getNickname(), 
				card.getBank(), 
				card.getEndnumbers(),
				card.getBillingPeriodStart(), 
				card.getBillingPeriodEnd(), 
				card.getTotalLimit(),
				card.getDataCadastro(),
				Optional.empty());
	}

}
