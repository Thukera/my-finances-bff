package com.thukera.creditcard.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thukera.creditcard.model.dto.InvoiceDTO;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.enums.InvoiceStatus;
import com.thukera.creditcard.model.form.InvoiceForm;
import com.thukera.creditcard.repository.InvoiceRepository;
import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.service.AuthenticationHelper;

/**
 * Service layer for Invoice retrieval and management
 * Handles invoice queries with proper authorization
 */
@Service
public class InvoiceService {

    private static final Logger logger = LogManager.getLogger(InvoiceService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private AuthenticationHelper authHelper;
    
 // GET INVOICE BY ID - Entity
    @Transactional(readOnly = true)
    public Invoice getInvoiceEntityById(Long invoiceId) {
        logger.debug("### Fetching invoice with ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        validateInvoiceOwnership(invoice);

        logger.debug("### Invoice access authorized");
        return invoice;
    }

    // GET INVOICE BY ID - DTO
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long invoiceId) {
        logger.debug("### Fetching invoice with ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        validateInvoiceOwnership(invoice);

        logger.debug("### Invoice access authorized");
        return InvoiceDTO.fromEntity(invoice);
    }


    // GET CURRETNT INVOICE BY DATE AND CARD ID- DTO 
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
    
 // UPDATE INVOICE 
    @Transactional(readOnly = true)
    public InvoiceDTO putInvoice(Long invoiceId, InvoiceForm invoiceForm) {
        
    	logger.debug("### Form: {}", invoiceForm.toString());
    	Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
    	
    	validateInvoiceOwnership(invoice);
        logger.debug("### Invoice Selected: {}", invoice.getInvoiceId());
        
        invoice.setEstimateLimit(invoiceForm.getEstimateLimit());
        logger.debug("### Invoice Updated: {}", invoice.toString());
        invoiceRepository.save(invoice);
        
        return InvoiceDTO.fromEntity(invoice);
    }
    
    // CHANGE INVOICE STATUS 
    @Transactional
    public InvoiceDTO putInvoiceStatus(Long invoiceId, String status) {
        
    	Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
    	
    	validateInvoiceOwnership(invoice);
        logger.debug("### Invoice Selected: {}", invoice.getInvoiceId());
        
        try {
        	InvoiceStatus newStatus = InvoiceStatus.valueOf(status.toUpperCase());	
			invoice.setStatus(newStatus);
			invoiceRepository.save(invoice);
			logger.debug("### Invoice status updated to: {}", newStatus);
		} catch (IllegalArgumentException e) {
			logger.error("### Invalid status value: {}", status);
			throw new IllegalArgumentException("Invalid status value: " + status);
		}
        return InvoiceDTO.fromEntity(invoice);
    }
    

    // VALIDATE OWNERSHIP
    private void validateInvoiceOwnership(Invoice invoice) {
        Long cardOwnerId = invoice.getCreditCard().getUser().getId();
        if (!authHelper.canAccessUserResource(cardOwnerId)) {
            logger.warn("### Unauthorized access attempt to invoice: {}", invoice.getInvoiceId());
            throw new SecurityException("Não autorizado");
        }
    }
}
