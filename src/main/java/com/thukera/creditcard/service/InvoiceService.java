package com.thukera.creditcard.service;

import java.time.LocalDate;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thukera.creditcard.controller.CreditcardController;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.enums.InvoiceStatus;
import com.thukera.creditcard.repository.InvoiceRepository;

@Service
public class InvoiceService {

	private static final Logger logger = LogManager.getLogger(InvoiceService.class);

	
    @Autowired
    private InvoiceRepository invoiceRepository;

    public Invoice getOrCreateCurrentInvoice(CreditCard creditCard) {
    	
    	logger.debug("----------- ----- INVOICE SERVICE ----- -----------");
    	
        LocalDate today = LocalDate.now();
        logger.debug("## Today {}", today);

        // 1) Check for current OPEN invoice
        Optional<Invoice> openInvoice = invoiceRepository.findByCreditCardAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(creditCard, InvoiceStatus.OPEN, today, today);
        if (openInvoice.isPresent()) {
        	logger.debug("## Opened Invoice is Present : {}", openInvoice.toString());
            return openInvoice.get();
        }

        logger.debug("## Invoice not Present ! ");
        // 2) Check for a PENDING invoice that should be opened now
        Optional<Invoice> pendingInvoice = invoiceRepository.findByCreditCardAndStatus(creditCard, InvoiceStatus.PENDING);

        if (pendingInvoice.isPresent()) {
        	logger.debug("## Pending Invoice is Present : {}", openInvoice.toString());
            Invoice invoice = pendingInvoice.get();
            if (!today.isBefore(invoice.getStartDate()) && !today.isAfter(invoice.getEndDate())) {
                invoice.setStatus(InvoiceStatus.OPEN);
                return invoiceRepository.save(invoice);
            }
        }

        logger.debug("## Create Invoice! ");
        // 3) Create new invoice
        Invoice newInvoice = new Invoice();
        newInvoice.setCreditCard(creditCard);
        newInvoice.setDueDate(calculateDueDate(today, creditCard.getDueDate()));
        newInvoice.setStartDate(calculateStartDate(today, creditCard.getBillingPeriodStart()));
        newInvoice.setEndDate(calculateEndDate(today, creditCard.getBillingPeriodEnd()));
        newInvoice.setStatus(InvoiceStatus.OPEN);
        
        logger.debug("## New Invoice : {} ",newInvoice.toString());
        return invoiceRepository.save(newInvoice);
    }

    private LocalDate calculateStartDate(LocalDate today, int startDate) {
        // e.g., start today or card’s billing cycle
        return today.withDayOfMonth(1);
    }

    private LocalDate calculateEndDate(LocalDate today, int endDate) {
        // example: end of month
        return today.withDayOfMonth(endDate);
    }
    
    private LocalDate calculateDueDate(LocalDate today , int dueDate) {
        // example: end of month
        return today.withDayOfMonth(dueDate);
    }
}

