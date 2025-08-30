package com.thukera.creditcard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thukera.creditcard.controller.CreditcardController;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.CreditPurchase;
import com.thukera.creditcard.model.entities.Installment;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.entities.PurchaseCategory;
import com.thukera.creditcard.model.enums.InvoiceStatus;
import com.thukera.creditcard.model.form.CreditPurchaseForm;
import com.thukera.creditcard.repository.CreditPurchaseRepository;
import com.thukera.creditcard.repository.InstallmentRepository;
import com.thukera.creditcard.repository.InvoiceRepository;
import com.thukera.creditcard.repository.PurchaseCategoryRepository;

import jakarta.transaction.Transactional;

@Service
public class InvoiceService {

	private static final Logger logger = LogManager.getLogger(InvoiceService.class);

	
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private InstallmentRepository installmentRepository;
    
    @Autowired
	private CreditPurchaseRepository purchaseRepository;
    
    @Autowired
    private PurchaseCategoryRepository purchaseCategoryRepository;

    public Invoice getOrCreateCurrentInvoice(CreditCard creditCard) {
    	
    	logger.debug("----------- ----- INVOICE SERVICE ----- -----------");
    	
        LocalDate today = LocalDate.now();
        logger.debug("## Today {}", today);

        // 1) Check for current OPEN invoice
        Optional<Invoice> openInvoice = invoiceRepository.findByCreditCardAndStatus(creditCard, InvoiceStatus.OPEN);
        if (openInvoice.isPresent()) {
        	logger.debug("## Opened Invoice is Present : {}", openInvoice.toString());
        	
        	if (openInvoice.get().getEndDate().isBefore(today)) {
        		logger.debug("## Opened Invoice Must Close");
        		openInvoice.get().setStatus(InvoiceStatus.CLOSED);
        		invoiceRepository.save(openInvoice.get());	
        	} else {
        		return openInvoice.get();
        	}       
        }
        logger.debug("## Open Invoice not Present !");    
        
        
        // 2) Check for a PENDING invoice that should be opened now
        Optional<Invoice> pendingInvoice = invoiceRepository.findFirstByCreditCardAndStatusOrderByStartDateAsc(creditCard,InvoiceStatus.PENDING);
        
        
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
        newInvoice.setTotalAmount(BigDecimal.ZERO);
        newInvoice.setStatus(InvoiceStatus.OPEN);
        
        logger.debug("## New Invoice : {} ",newInvoice.toString());
        return invoiceRepository.save(newInvoice);
    }
    
    @Transactional
    public CreditPurchase createPurchaseWithInstallments(CreditPurchase purchase, int totalInstallments) {
        
        BigDecimal installmentValue = purchase.getValue()
                                              .divide(BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_UP);

        LocalDate billingStart = purchase.getPurchaseDateTime().toLocalDate();
        CreditCard card = purchase.getCreditCard();

        for (int i = 1; i <= totalInstallments; i++) {
            // Shift billing period for each installment
            LocalDate cycleDate = billingStart.plusMonths(i - 1);

            // Start / end / due using your existing helpers
            LocalDate installmentStartDate = calculateStartDate(cycleDate, card.getBillingPeriodStart());
            LocalDate installmentEndDate   = calculateEndDate(cycleDate, card.getBillingPeriodEnd());
            LocalDate dueDate              = calculateDueDate(cycleDate, card.getDueDate());

            // Now use these dates for invoice + installment
            Invoice invoice = findOrCreateInvoice(card, installmentStartDate, installmentEndDate, dueDate);

            Installment installment = new Installment();
            installment.setPurchase(purchase);
            installment.setNumber(i);
            installment.setDueDate(dueDate);
            installment.setValue(installmentValue);
            installment.setInvoice(invoice);

            installmentRepository.save(installment);

            invoice.setTotalAmount(invoice.getTotalAmount().add(installmentValue));
            invoiceRepository.save(invoice);
        }

        return purchaseRepository.save(purchase);
    }
    
    private Invoice findOrCreateInvoice(CreditCard card, LocalDate startDate, LocalDate endDate, LocalDate dueDate) {
        return invoiceRepository
                .findByCreditCardAndStartDateAndEndDate(card, startDate, endDate)
                .orElseGet(() -> {
                    Invoice invoice = new Invoice();
                    invoice.setCreditCard(card);
                    invoice.setStartDate(startDate);
                    invoice.setEndDate(endDate);
                    invoice.setDueDate(dueDate);
                    invoice.setStatus(InvoiceStatus.PENDING);
                    invoice.setTotalAmount(BigDecimal.ZERO);
                    return invoiceRepository.save(invoice);
                });
    }



    private LocalDate calculateStartDate(LocalDate today, int startDate) {
        // e.g., start today or card’s billing cycle
        return today.withDayOfMonth(startDate);
    }

    private LocalDate calculateEndDate(LocalDate today, int endDate) {
    	LocalDate invoiceEndDate = today.withDayOfMonth(endDate);
    	invoiceEndDate = invoiceEndDate.plusMonths(1);
        // example: end of month
        return invoiceEndDate;
    }
    
    private LocalDate calculateDueDate(LocalDate today , int dueDate) {
    	LocalDate invoiceDueDate = today.withDayOfMonth(dueDate);
    	invoiceDueDate = invoiceDueDate.plusMonths(1);
        // example: end of month
        return invoiceDueDate;
    }
    
    @Transactional
    public CreditPurchase createPurchase(CreditPurchaseForm purchaseForm, CreditCard creditCard) {

        // Lookup or create category
        PurchaseCategory category = purchaseCategoryRepository
                .findByName(purchaseForm.getCategory())
                .orElseGet(() -> purchaseCategoryRepository.save(new PurchaseCategory(purchaseForm.getCategory(), false)));

        // Build purchase entity
        CreditPurchase purchase = new CreditPurchase();
        purchase.setDescricao(purchaseForm.getDescricao());
        purchase.setValue(purchaseForm.getValue());
        purchase.setTotalInstallments(purchaseForm.getTotalInstallments());
        purchase.setCategory(category);
        purchase.setCreditCard(creditCard);

        Invoice currentInvoice = getOrCreateCurrentInvoice(creditCard);

        if (purchaseForm.getTotalInstallments() == 1) {
            // Single installment → add to current invoice
            purchase.setInvoice(currentInvoice);
            currentInvoice.getPurchases().add(purchase);
            invoiceRepository.save(currentInvoice); // cascades to purchase
            return purchase;

        } else {
            // Multiple installments → create one CreditPurchase per installment
            BigDecimal installmentValue = purchase.getValue().divide(BigDecimal.valueOf(purchaseForm.getTotalInstallments()), 2, RoundingMode.HALF_UP);
            
            
            // >>>>>>>>>>>>> !!!!!!!!!!!!! Fix This Logic !!!!!!!!!!!! <<<<<<<<<<<<<<<<<< 
            for (int i = 0; i < purchaseForm.getTotalInstallments(); i++) {
                CreditPurchase installment = new CreditPurchase();
                installment.setDescricao(purchase.getDescricao() + " (" + (i + 1) + "/" + purchaseForm.getTotalInstallments() + ")");
                installment.setValue(installmentValue);
                installment.setTotalInstallments(1); // each "installment" is a single purchase now
                installment.setCategory(category);
                installment.setCreditCard(creditCard);
                installment.setInvoice(currentInvoice);
                
                currentInvoice.getPurchases().add(installment);
            }

            invoiceRepository.save(currentInvoice); // cascades all installments
            return purchase; // or return the first installment if needed
        }
    }



}

