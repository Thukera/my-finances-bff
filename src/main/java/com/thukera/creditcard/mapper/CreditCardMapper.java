package com.thukera.creditcard.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.thukera.creditcard.model.dto.InvoiceDTOFromCreditCard;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.form.CreditCardForm;
import com.thukera.user.model.entities.User;

/**
 * Mapper class for CreditCard entity transformations
 * Separates mapping logic from business logic
 */
@Component
public class CreditCardMapper {

    /**
     * Convert CreditCardForm to CreditCard entity
     * @param form the input form
     * @param user the owner user
     * @return CreditCard entity
     */
    public CreditCard toEntity(CreditCardForm form, User user) {
        CreditCard card = new CreditCard();
        card.setUser(user);
        card.setBank(form.getBank());
        card.setNickname(form.getNickname());
        card.setEndnumbers(form.getEndNumbers());
        card.setDueDate(form.getDueDate());
        card.setBillingPeriodStart(form.getBillingPeriodStart());
        card.setBillingPeriodEnd(form.getBillingPeriodEnd());
        card.setTotalLimit(form.getTotalLimit());
        card.setEstimateLimit(form.getEstimateLimit());
        
        return card;
    }

    /**
     * Convert CreditCard entity to CreditCardForm DTO
     * @param entity the CreditCard entity
     * @return CreditCardForm DTO
     */
    public CreditCardForm toForm(CreditCard entity) {
        return new CreditCardForm(
            entity.getCardId(),
            entity.getBank(),
            entity.getEndnumbers(),
            entity.getNickname(),
            entity.getBillingPeriodStart(),
            entity.getBillingPeriodEnd(),
            entity.getUsedLimit(),
            entity.getTotalLimit(),
            entity.getEstimateLimit(),
            entity.getDataCadastro(),
            entity.getInvoices().stream()
                .map(InvoiceDTOFromCreditCard::fromEntity)
                .collect(Collectors.toList())
        );
    }

    /**
     * Update existing entity with form data
     * @param entity existing entity
     * @param form updated form data
     */
    public void updateEntity(CreditCard entity, CreditCardForm form) {
        if (form.getBank() != null) {
            entity.setBank(form.getBank());
        }
        if (form.getNickname() != null) {
            entity.setNickname(form.getNickname());
        }
        if (form.getEndNumbers() != null) {
            entity.setEndnumbers(form.getEndNumbers());
        }
        if (form.getDueDate() != 0) {
            entity.setDueDate(form.getDueDate());
        }
        if (form.getBillingPeriodStart() != 0) {
            entity.setBillingPeriodStart(form.getBillingPeriodStart());
        }
        if (form.getBillingPeriodEnd() != 0) {
            entity.setBillingPeriodEnd(form.getBillingPeriodEnd());
        }
        if (form.getTotalLimit() != null) {
            entity.setTotalLimit(form.getTotalLimit());
        }
        if (form.getEstimateLimit() != null) {
            entity.setEstimateLimit(form.getEstimateLimit());
        }
    }
}
