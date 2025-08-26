package com.thukera.creditcard.model.enums;

import com.thukera.user.model.enums.RoleName;

public enum PurchaseCategoryEnum {
	ASSINATURAS("assinatura"),
	TRANSPORTE("transporte"), 
	CARRO("carro"),
	RESTAURANTE("restaurante"),
	MERCADO("mercado"),
	BALADA("balada"),
	CASA_E_COZINHA("casa_cozinha"),
	GAMES("games"),
	ROUPAS("roupas"),
	ESTUDOS("estudos"),
	FARMACIA("farmacia"),
	OUTROS("outros");

	private String key;

	PurchaseCategoryEnum(String key) {
		this.key = key;
	}

	public static PurchaseCategoryEnum getRole(String key) {
		for (PurchaseCategoryEnum n : values()) {
			if (n.key.equalsIgnoreCase(key)) {
				return n;
			}
		}
		return null;

	}

}
