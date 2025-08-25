package com.thukera.creditcard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.thukera.creditcard.model.entities.CreditCard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardDTO {
    private Long id;
    private String bank;
    private String endnumbers;
    private Integer billingPeriodStart;
    private Integer billingPeriodEnd;
    private BigDecimal totalLimit;
    private LocalDate dataCadastro;

    public static CreditCardDTO fromEntity(CreditCard card) {
        return new CreditCardDTO(
            card.getCardId(),
            card.getBank(),
            card.getEndnumbers(),
            card.getBillingPeriodStart(),
            card.getBillingPeriodEnd(),
            card.getTotalLimit(),
            card.getDataCadastro()
        );
    }
}

