package com.thukera.creditcard.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thukera.creditcard.model.dto.InvoiceDTO;
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
     * Delete a purchase and clean up all related data
     * - Removes purchase from all related invoices
     * - Removes all installments from their invoices
     * - Updates invoice totals
     * - Updates credit card's used limit
     * - Deletes the purchase
     * @param purchaseId the purchase ID to delete
     * @return true if deleted successfully
     * @throws NotFoundException if purchase not found or has no invoice
     * @throws SecurityException if user doesn't own the purchase
     */
    @Transactional
    public void deletePurchase(Long purchaseId) {
        logger.debug("### Deleting purchase with ID: {}", purchaseId);

        // 1. Find and validate purchase
        CreditPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));
     
        // Get first invoice to check ownership
        Invoice firstInvoice = purchase.getInvoices().stream().findFirst()
                .orElseThrow(() -> new NotFoundException("No invoice linked to this purchase"));
        
        // Validate ownership through the invoice's credit card
        Long cardOwnerId = firstInvoice.getCreditCard().getUser().getId();
        if (!authHelper.canAccessUserResource(cardOwnerId)) {
            logger.warn("### Unauthorized access attempt to purchase: {}", purchaseId);
            throw new SecurityException("Não autorizado");
        }
        logger.debug("### Purchase ownership validated");

        // Get the credit card for later update
        CreditCard creditCard = firstInvoice.getCreditCard();
        
        // 2. Collect all affected invoices before deletion
        List<Invoice> affectedInvoices = new ArrayList<>(purchase.getInvoices());
        logger.debug("### Purchase is linked to {} invoice(s)", affectedInvoices.size());
        
        // 3. Remove purchase from all related invoices (many-to-many relationship cleanup)
        logger.debug("### Removing purchase from invoice relationships");
        for (Invoice invoice : affectedInvoices) {
            invoice.getPurchases().remove(purchase);
            logger.debug("### Removed purchase from invoice {}", invoice.getInvoiceId());
        }
        
        // Clear the invoice list from purchase side
        purchase.getInvoices().clear();

        // 4. Delete the purchase (cascade will handle installments deletion)
        purchaseRepository.delete(purchase);
        logger.debug("### Purchase deleted from database");

        // 5. Update invoice totals (recalculates from remaining purchases/installments)
        logger.debug("### Recalculating totals for affected invoices");
        for (Invoice invoice : affectedInvoices) {
            invoiceService.updateTotalAmount(InvoiceDTO.fromEntity(invoice));
            logger.debug("### Recalculated total for invoice {}", invoice.getInvoiceId());
        }

        // 6. Update credit card's used limit
        creditCardService.updateCreditCardUsedLimit(creditCard);
        logger.debug("### Credit card used limit updated");

        logger.debug("### Purchase {} successfully deleted", purchaseId);
        
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

    /**
     * Update a purchase and recalculate all related data
     * - Updates purchase details (description, value, category)
     * - Recalculates installments if value or installment count changes
     * - Reassigns to different invoices if purchase date changes
     * - Updates invoice totals
     * - Updates credit card's used limit
     * @param purchaseId the purchase ID to update
     * @param purchaseForm the updated purchase data
     * @return PurchaseDTO with updated data
     * @throws NotFoundException if purchase not found or has no invoice
     * @throws SecurityException if user doesn't own the purchase
     */
    @Transactional
    public PurchaseDTO updatePurchaseById(Long purchaseId, CreditPurchaseForm purchaseForm) {
        logger.debug("### Updating purchase with ID: {}", purchaseId);
        logger.debug("### Update form: {}", purchaseForm);

        // 1. Find and validate purchase
        CreditPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));
     
        // Get first invoice to check ownership
        Invoice firstInvoice = purchase.getInvoices().stream().findFirst()
                .orElseThrow(() -> new NotFoundException("No invoice linked to this purchase"));
        
        // Validate ownership through the invoice's credit card
        Long cardOwnerId = firstInvoice.getCreditCard().getUser().getId();
        if (!authHelper.canAccessUserResource(cardOwnerId)) {
            logger.warn("### Unauthorized access attempt to purchase: {}", purchaseId);
            throw new SecurityException("Não autorizado");
        }
        logger.debug("### Purchase ownership validated");

        // Get the credit card
        CreditCard creditCard = firstInvoice.getCreditCard();
        
        // 2. Store old values for comparison
        BigDecimal oldValue = purchase.getValue();
        boolean valueChanged = purchaseForm.getValue() != null && 
                              oldValue.compareTo(purchaseForm.getValue()) != 0;
        
        // Collect affected invoices before any changes
        List<Invoice> oldInvoices = new ArrayList<>(purchase.getInvoices());
        
        // 3. Check if installments structure needs to change
        boolean installmentsChanged = false;
        int currentInstallmentCount = purchase.isHasInstallments() ? 
                                      purchase.getInstallments().size() : 1;
        int newInstallmentCount = purchaseForm.getTotalInstallments() > 0 ? 
                                 purchaseForm.getTotalInstallments() : 1;
        
        if (currentInstallmentCount != newInstallmentCount || valueChanged) {
            installmentsChanged = true;
            logger.debug("### Installments structure changed - old count: {}, new count: {}, value changed: {}", 
                        currentInstallmentCount, newInstallmentCount, valueChanged);
        }
        
        // 4. If installments changed significantly, we need to recreate the purchase structure
        if (installmentsChanged) {
            logger.debug("### Recreating purchase with new installment structure");
            
            // Remove from old invoices and clear installments
            for (Invoice invoice : oldInvoices) {
                invoice.getPurchases().remove(purchase);
            }
            purchase.getInvoices().clear();
            purchase.getInstallments().clear();
            
            // Update basic purchase info
            if (purchaseForm.getDescricao() != null) {
                purchase.setDescricao(purchaseForm.getDescricao());
            }
            if (purchaseForm.getValue() != null) {
                purchase.setValue(purchaseForm.getValue());
            }
            if (purchaseForm.getPurchaseDateTime() != null) {
                purchase.setPurchaseDateTime(purchaseForm.getPurchaseDateTime());
            }
            
            // Update category if provided
            if (purchaseForm.getCategory() != null) {
                var category = invoiceService.findOrCreateCategory(purchaseForm.getCategory());
                purchase.setCategory(category);
            }
            
            // Recreate the invoice/installment structure
            purchase.setHasInstallments(newInstallmentCount > 1);
            invoiceService.reassignPurchaseToInvoices(purchase, newInstallmentCount, creditCard);
            
            // Save the updated purchase
            purchaseRepository.save(purchase);
            
            // Update totals for old invoices
            logger.debug("### Updating old invoice totals");
            for (Invoice invoice : oldInvoices) {
                invoiceService.updateTotalAmount(InvoiceDTO.fromEntity(invoice));
            }
            
            // Update totals for new invoices
            logger.debug("### Updating new invoice totals");
            for (Invoice invoice : purchase.getInvoices()) {
                invoiceService.updateTotalAmount(InvoiceDTO.fromEntity(invoice));
            }
            
        } else {
            // 5. Simple update - just description and/or category
            logger.debug("### Simple update - no installment changes");
            
            boolean updated = false;
            if (purchaseForm.getDescricao() != null && 
                !purchaseForm.getDescricao().equals(purchase.getDescricao())) {
                purchase.setDescricao(purchaseForm.getDescricao());
                updated = true;
                logger.debug("### Updated description");
            }
            
            if (purchaseForm.getCategory() != null) {
                var category = invoiceService.findOrCreateCategory(purchaseForm.getCategory());
                if (!category.equals(purchase.getCategory())) {
                    purchase.setCategory(category);
                    updated = true;
                    logger.debug("### Updated category");
                }
            }
            
            if (updated) {
                purchaseRepository.save(purchase);
            }
        }
        
        // 6. Update credit card's used limit (always recalculate after any change)
        creditCardService.updateCreditCardUsedLimit(creditCard);
        logger.debug("### Credit card used limit updated");

        logger.debug("### Purchase {} successfully updated", purchaseId);
        return PurchaseDTO.fromEntity(purchase);
    }
}
