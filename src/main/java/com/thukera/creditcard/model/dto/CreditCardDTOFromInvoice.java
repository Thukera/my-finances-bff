package com.thukera.creditcard.model.dto;


import com.thukera.creditcard.model.entities.CreditCard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardDTOFromInvoice {

	private Long id;
	private String nickname;

	public static CreditCardDTOFromInvoice fromEntity(CreditCard card) {
		return new CreditCardDTOFromInvoice(card.getCardId(), card.getNickname());
	}
}
