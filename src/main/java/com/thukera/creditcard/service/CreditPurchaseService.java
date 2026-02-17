package com.thukera.creditcard.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thukera.creditcard.model.dto.PurchaseDTO;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.CreditPurchase;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.form.CreditPurchaseForm;
import com.thukera.creditcard.repository.CreditPurchaseRepository;
import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.service.AuthenticationHelper;

/**
 * Service layer for CreditPurchase business logic
 * Handles purchase creation and retrieval with proper authorization
 */
@Service
public class CreditPurchaseService {

    private static final Logger logger = LogManager.getLogger(CreditPurchaseService.class);

    @Autowired
    private CreditPurchaseRepository purchaseRepository;

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private CreditTransactionService invoiceService;

    @Autowired
    private AuthenticationHelper authHelper;

    /**
     * Create a new purchase and add it to appropriate invoice(s)
     * Updates the credit card's used limit
     * @param purchaseForm the purchase form data
     * @return PurchaseDTO
     */
    @Transactional
    public PurchaseDTO createPurchase(CreditPurchaseForm purchaseForm) {
        logger.debug("### Creating purchase: {}", purchaseForm);

        // Get and validate credit card ownership
        CreditCard creditCard = creditCardService.getCreditCardEntityById(purchaseForm.getCreditCardId());
        logger.debug("### CreditCard validated: {}", creditCard.getCardId());

        // Create purchase and add to invoice(s)
        CreditPurchase newPurchase = invoiceService.createPurchase(purchaseForm, creditCard);
        logger.debug("### Purchase created: {}", newPurchase.getPurchaseId());

        // Update credit card's used limit
        creditCard.setUsedLimit(creditCard.getUsedLimit().add(newPurchase.getValue()));
        creditCardService.updateCreditCard(creditCard);

        return PurchaseDTO.fromEntity(newPurchase);
    }

    /**
     * Get purchase details by ID
     * Validates that user owns the credit card associated with the purchase
     * @param purchaseId the purchase ID
     * @return PurchaseDTO
     * @throws NotFoundException if purchase not found or has no invoice
     * @throws SecurityException if user doesn't own the purchase
     */
    @Transactional(readOnly = true)
    public PurchaseDTO getPurchaseById(Long purchaseId) {
        logger.debug("### Fetching purchase with ID: {}", purchaseId);

        CreditPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));

        // Get first invoice to check ownership
        Invoice invoice = purchase.getInvoices().stream().findFirst()
                .orElseThrow(() -> new NotFoundException("No invoice linked to this purchase"));

        // Validate ownership through the invoice's credit card
        Long cardOwnerId = invoice.getCreditCard().getUser().getId();
        if (!authHelper.canAccessUserResource(cardOwnerId)) {
            logger.warn("### Unauthorized access attempt to purchase: {}", purchaseId);
            throw new SecurityException("Não autorizado");
        }

        logger.debug("### Purchase access authorized");
        return PurchaseDTO.fromEntity(purchase);
    }
}
