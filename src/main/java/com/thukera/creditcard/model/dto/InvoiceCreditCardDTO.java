package com.thukera.creditcard.model.dto;


import com.thukera.creditcard.model.entities.CreditCard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceCreditCardDTO {

	private Long id;
	private String nickname;

	public static InvoiceCreditCardDTO fromEntity(CreditCard card) {
		return new InvoiceCreditCardDTO(card.getCardId(), card.getNickname());
	}
}
