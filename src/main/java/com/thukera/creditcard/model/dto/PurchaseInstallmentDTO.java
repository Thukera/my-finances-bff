package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;


import com.thukera.creditcard.model.entities.Installment;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseInstallmentDTO {

	private Long installmentId;
	private int currentInstallment;
	private int totalInstallment;
	private BigDecimal value;

	public static PurchaseInstallmentDTO fromEntity(Installment installment) {
		return new PurchaseInstallmentDTO(installment.getInstallmentId(), installment.getCurrentInstallment(),
				installment.getTotalInstallment(), installment.getValue());
	}

}
