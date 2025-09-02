package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;


import com.thukera.creditcard.model.entities.Installment;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstallmentDTOFromPurchase {

	private Long installmentId;
	private int currentInstallment;
	private int totalInstallment;
	private BigDecimal value;
	private InvoiceDTOFromPurchase invoice;


	public static InstallmentDTOFromPurchase fromEntity(Installment installment) {
		return new InstallmentDTOFromPurchase(
			installment.getInstallmentId(), 
			installment.getCurrentInstallment(),
			installment.getTotalInstallment(), 
			installment.getValue(),
			InvoiceDTOFromPurchase.fromEntity(installment.getInvoice())
		);
	}

}
