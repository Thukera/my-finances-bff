package com.thukera.creditcard.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thukera.creditcard.model.dto.InvoiceDTO;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.repository.InvoiceRepository;
import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.service.AuthenticationHelper;

/**
 * Service layer for Invoice retrieval and management
 * Handles invoice queries with proper authorization
 */
@Service
public class InvoiceManagementService {

    private static final Logger logger = LogManager.getLogger(InvoiceManagementService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private AuthenticationHelper authHelper;

    /**
     * Get invoice details by ID
     * Validates ownership before returning
     * @param invoiceId the invoice ID
     * @return InvoiceDTO
     * @throws NotFoundException if invoice not found
     * @throws SecurityException if user doesn't own the invoice
     */
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long invoiceId) {
        logger.debug("### Fetching invoice with ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        validateInvoiceOwnership(invoice);

        logger.debug("### Invoice access authorized");
        return InvoiceDTO.fromEntity(invoice);
    }

    /**
     * Get current invoice for a credit card
     * @param cardId the credit card ID
     * @return InvoiceDTO
     * @throws NotFoundException if card not found or no current invoice exists
     * @throws SecurityException if user doesn't own the card
     */
    @Transactional(readOnly = true)
    public InvoiceDTO getCurrentInvoice(Long cardId) {
        logger.debug("### Fetching current invoice for card ID: {}", cardId);

        // This validates ownership
        CreditCard card = creditCardService.getCreditCardEntityById(cardId);

        // Find current invoice based on today's date
        Invoice invoice = invoiceRepository.findTargetInvoice(cardId, java.time.LocalDate.now())
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        logger.debug("### Current invoice found: {}", invoice.getInvoiceId());
        return InvoiceDTO.fromEntity(invoice);
    }

    /**
     * Validate that the current user owns the invoice's credit card
     * Admins can access any invoice
     * @param invoice the invoice to validate
     * @throws SecurityException if user doesn't own the invoice
     */
    private void validateInvoiceOwnership(Invoice invoice) {
        Long cardOwnerId = invoice.getCreditCard().getUser().getId();
        if (!authHelper.canAccessUserResource(cardOwnerId)) {
            logger.warn("### Unauthorized access attempt to invoice: {}", invoice.getInvoiceId());
            throw new SecurityException("Não autorizado");
        }
    }
}
