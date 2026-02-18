package com.thukera.creditcard.service;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thukera.creditcard.mapper.CreditCardMapper;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.form.CreditCardForm;
import com.thukera.creditcard.repository.CreditcardRepository;
import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.model.entities.User;
import com.thukera.user.service.AuthenticationHelper;

/**
 * Service layer for CreditCard business logic
 * Handles all business operations related to credit cards
 */
@Service
public class CreditCardService {

    private static final Logger logger = LogManager.getLogger(CreditCardService.class);

    @Autowired
    private CreditcardRepository creditcardRepository;

    @Autowired
    private CreditCardMapper creditCardMapper;

    @Autowired
    private AuthenticationHelper authHelper;

    /**
     * Create a new credit card for the current user
     * @param form the credit card form data
     * @return saved CreditCardForm
     */
    @Transactional
    public CreditCardForm createCreditCard(CreditCardForm form) {
        logger.debug("### Creating credit card: {}", form);

        User currentUser = authHelper.getCurrentUser();
        logger.debug("### User: {}", currentUser.getUsername());

        CreditCard card = creditCardMapper.toEntity(form, currentUser);
        logger.debug("### CreditCard entity created: {}", card);

        CreditCard savedCard = creditcardRepository.save(card);
        logger.debug("### CreditCard saved with ID: {}", savedCard.getCardId());

        return creditCardMapper.toForm(savedCard);
    }

    /**
     * Get credit card details by ID
     * Validates ownership before returning
     * @param cardId the card ID
     * @return CreditCardForm
     * @throws NotFoundException if card not found
     * @throws SecurityException if user doesn't own the card
     */
    @Transactional(readOnly = true)
    public CreditCardForm getCreditCardById(Long cardId) {
        logger.debug("### Fetching credit card with ID: {}", cardId);

        CreditCard card = creditcardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Cartão não encontrado"));

        validateCardOwnership(card);

        return creditCardMapper.toForm(card);
    }

    /**
     * Get credit card entity by ID with ownership validation
     * @param cardId the card ID
     * @return CreditCard entity
     * @throws NotFoundException if card not found
     * @throws SecurityException if user doesn't own the card
     */
    @Transactional(readOnly = true)
    public CreditCard getCreditCardEntityById(Long cardId) {
        logger.debug("### Fetching credit card entity with ID: {}", cardId);

        CreditCard card = creditcardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Cartão não encontrado"));

        validateCardOwnership(card);

        return card;
    }

    @Transactional
    public void updateCreditCard(CreditCard card) {
        logger.debug("### Updating credit card: {}", card.getCardId());
        creditcardRepository.save(card);
    }
    
    @Transactional
    public CreditCardForm updateCreditCard(CreditCard card, CreditCardForm form) {
        logger.debug("### Updating credit card: {}", card.getCardId());
        validateCardOwnership(card);
          
        CreditCard updatedCard = creditCardMapper.updateEntity(card, form);
        creditcardRepository.save(updatedCard);
		logger.debug("### CreditCard updated: {}", updatedCard.getCardId());
        
        return creditCardMapper.toForm(updatedCard);
    }
    
    @Transactional
    public CreditCardForm updateCreditCardUsedLimit(CreditCard card) {
        logger.debug("### Updating credit card used limit: {}", card.getCardId());
        validateCardOwnership(card);
          
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (var invoice : card.getInvoices()) {
			if (invoice.getStatus() != com.thukera.creditcard.model.enums.InvoiceStatus.PAYD || invoice.getStatus() != com.thukera.creditcard.model.enums.InvoiceStatus.OPEN) {
				totalAmount = totalAmount.add(invoice.getTotalAmount());
			}
		}
        
        if (totalAmount.compareTo(card.getUsedLimit()) != 0) {
			card.setUsedLimit(totalAmount);
        	creditcardRepository.save(card);
        	logger.debug("### Total Amount updated: {}", totalAmount);
		} else {	
			logger.debug("### Used Limit ok");
		}         
        return creditCardMapper.toForm(card);
    }

    private void validateCardOwnership(CreditCard card) {
        if (!authHelper.canAccessUserResource(card.getUser().getId())) {
            logger.warn("### Unauthorized access attempt to card: {}", card.getCardId());
            throw new SecurityException("Cartão não pertence ao usuário");
        }
    }
}
