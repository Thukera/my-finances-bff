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
public class CreditCardUserDTO {
    private Long id;
    private String nickname;
    private String bank;
    private String endnumbers;
    private Integer billingPeriodStart;
    private Integer billingPeriodEnd;
    private BigDecimal totalLimit;
    private LocalDate dataCadastro;

    public static CreditCardUserDTO fromEntity(CreditCard card) {
        return new CreditCardUserDTO(
            card.getCardId(),
            card.getNickname(),
            card.getBank(),
            card.getEndnumbers(),
            card.getBillingPeriodStart(),
            card.getBillingPeriodEnd(),
            card.getTotalLimit(),
            card.getDataCadastro()
        );
    }
}

