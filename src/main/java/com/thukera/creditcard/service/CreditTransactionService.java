package com.thukera.creditcard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thukera.creditcard.model.dto.InvoiceDTO;
import com.thukera.creditcard.model.dto.PurchaseDTOFromInvoice;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.CreditPurchase;
import com.thukera.creditcard.model.entities.Installment;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.entities.PurchaseCategory;
import com.thukera.creditcard.model.enums.InvoiceStatus;
import com.thukera.creditcard.model.form.CreditPurchaseForm;
import com.thukera.creditcard.repository.CreditPurchaseRepository;
import com.thukera.creditcard.repository.InvoiceRepository;
import com.thukera.creditcard.repository.PurchaseCategoryRepository;

import jakarta.transaction.Transactional;

@Service
public class CreditTransactionService {

	private static final Logger logger = LogManager.getLogger(CreditTransactionService.class);

	
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private CreditPurchaseRepository creditPurchaseRepository;
    
    @Autowired
    private PurchaseCategoryRepository purchaseCategoryRepository;
  
    // ========================================================== PURCHASES METHODS ==========================================================
    //
    // --------------------------------------------------  CREATE AND INSERT NEW PURCHASE --------------------------------------------------  
    @Transactional
    public CreditPurchase createPurchase(CreditPurchaseForm purchaseForm, CreditCard creditCard) {
    	
    	logger.debug("## ==================================== ## INSERT PURCHASE ## ==================================== ## ");

        // Lookup or create category
        PurchaseCategory category = purchaseCategoryRepository
                .findByName(purchaseForm.getCategory())
                .orElseGet(() -> purchaseCategoryRepository.save(new PurchaseCategory(purchaseForm.getCategory(), false,false)));

        // Build purchase entity
        CreditPurchase purchase = new CreditPurchase(purchaseForm.getDescricao(),purchaseForm.getValue(),(purchaseForm.getPurchaseDateTime() != null) ? purchaseForm.getPurchaseDateTime() : LocalDateTime.now());
        purchase.setHasInstallments((purchaseForm.getTotalInstallments() > 1));
        purchase.setCategory(category);
        purchase.setCreditCard(creditCard);    
        logger.debug("## Purchase Class : {}",purchase.toString());

        // check if retroative : 
        boolean retroativePurchase = (purchaseForm.getPurchaseDateTime() != null) ? checkIfRetroative(purchaseForm.getPurchaseDateTime().toLocalDate(),creditCard) : false;
        logger.debug("## Retroative : {}",retroativePurchase);
        
        // Single installment → add to current invoice
        if (purchaseForm.getTotalInstallments() == 1) {
        	logger.debug("## Single Installment");
        	Invoice currentInvoice = retroativePurchase ? findOrCreateRetroativeInvoice(creditCard, purchaseForm) : getOrCreateCurrentInvoice(creditCard);
        	currentInvoice.setTotalAmount(currentInvoice.getTotalAmount().add(purchase.getValue()));
            currentInvoice.getPurchases().add(purchase);            
            purchase.getInvoices().add(currentInvoice);
            logger.debug("## Invoice : " + currentInvoice.toString());
            return creditPurchaseRepository.save(purchase);

        // For Multiple Installment/Invoices
        } else {
        	logger.debug("## Multiple Installment");
        	// calculate Installment
            BigDecimal installmentValue = purchase.getValue().divide(BigDecimal.valueOf(purchaseForm.getTotalInstallments()), 2, RoundingMode.HALF_UP);                       
            
            // Find or create current invoice
            Invoice currentInvoice = retroativePurchase ? findOrCreateRetroativeInvoice(creditCard, purchaseForm) : getOrCreateCurrentInvoice(creditCard);
        	currentInvoice.setTotalAmount(currentInvoice.getTotalAmount().add(installmentValue));
            currentInvoice.getPurchases().add(purchase);            
            purchase.getInvoices().add(currentInvoice);
            logger.debug("## First Invoice : " + currentInvoice.toString());
            
            LocalDate baseDate = retroativePurchase ? purchaseForm.getPurchaseDateTime().toLocalDate() : LocalDate.now();
            LocalDate installmentDate = purchaseForm.getPurchaseDateTime().toLocalDate();
            
            // Find or create next Invoices ; generate installments child
            ArrayList<Invoice> invoiceList = new ArrayList<Invoice>();
            ArrayList<Installment> installmentList = new ArrayList<Installment>();
            
            // Child Installment Handler
            Installment installment = new Installment(1, purchaseForm.getTotalInstallments(), installmentValue, purchase,currentInvoice);
            installmentList.add(installment);
            logger.debug("## First Installment : " + installment.toString());
            
            for (int i = 1; i < purchaseForm.getTotalInstallments(); i++) {
	
            	// Child Invoices Handler - Calculate dates from base date with proper month offset
                LocalDate dateForNextInvoice = baseDate.plusMonths(i);
                LocalDate nextStartDate = calculateStartDate(dateForNextInvoice, creditCard.getBillingPeriodStart());
                LocalDate nextEndDate   = calculateEndDate(dateForNextInvoice, creditCard.getBillingPeriodEnd());
                LocalDate nextdueDate   = calculateDueDate(dateForNextInvoice, creditCard.getDueDate());
                installmentDate = installmentDate.plusMonths(1);

                // Check if Retroative Invoice or reach real current
                Invoice nextInvoice = findOrCreateInvoice(creditCard, nextStartDate, nextEndDate, nextdueDate); 
                //Invoice nextInvoice = findOrCreateInvoice(creditCard, nextStartDate, nextEndDate, nextdueDate);
                nextInvoice.setTotalAmount(nextInvoice.getTotalAmount().add(installmentValue));
                nextInvoice.getPurchases().add(purchase);
                
                // check if is necessary according cascate structure
                invoiceRepository.save(nextInvoice);
                logger.debug("## Invoice " + i + " : " + nextInvoice.toString());
                invoiceList.add(nextInvoice);
  	
                // Child Installment Handler
                Installment nextInstallment = new Installment(i+1, purchaseForm.getTotalInstallments(), installmentValue, purchase,nextInvoice);
                installmentList.add(nextInstallment);
                logger.debug("## Installment " + i + " : " + installment.toString());
            }
            
            purchase.getInvoices().addAll(invoiceList);
            purchase.getInstallments().addAll(installmentList);
            logger.debug("### PURCHASE : {}", purchase.toString());
            return creditPurchaseRepository.save(purchase); 
        }
    }

    // -------------------------------- FIND AND ADD SIGNATURES ON CREDIT CARD -------------------------------- 
    private CreditPurchase creteRepeatedPurchase(CreditPurchase creditPurchase, Invoice currentInvoice) {
    	
    	 CreditPurchase purchase = new CreditPurchase();
         purchase.setDescricao(creditPurchase.getDescricao());
         purchase.setValue(creditPurchase.getValue());
         purchase.setHasInstallments(false);
         purchase.setCategory(creditPurchase.getCategory());
         purchase.setCreditCard(creditPurchase.getCreditCard());
         purchase.setPurchaseDateTime(creditPurchase.getPurchaseDateTime().plusMonths(1)); 
         purchase.getInvoices().add(currentInvoice);
         
         return creditPurchaseRepository.save(purchase);
    	
    }

    
    // =========================================== INVOICES METHODS ===================================================
    //
    // --------------------------------  DOUBLE CHECK INVOICE TOTAL AMOUNT  -------------------------------- 
    public Invoice updateTotalAmount(InvoiceDTO invoiceDTO) {  	
    	logger.debug("## ----------------------------- ## VEVIEW TOTAL AMOUNT BY PURCHASES COUNT ## ----------------------------- ## ");
    	Invoice invoice = invoiceRepository.findById(invoiceDTO.getInvoiceId()).orElseThrow(() -> new RuntimeException("Invoice not found"));
    	
    	// Recalculate total amount based on purchases and installments from DTO
    	BigDecimal totalAmount = BigDecimal.ZERO; 	
    	for (PurchaseDTOFromInvoice purchase : invoiceDTO.getPurchases()) {
			logger.debug("## Purchase : {}", purchase.toString());
			
			if(purchase.getInstallment() != null) {
				logger.debug("## Get Installment's Value");
				BigDecimal installmentValue = purchase.getInstallment().getValue();
				logger.debug("## Value to add : {}", installmentValue);
				totalAmount = totalAmount.add(installmentValue);
			} else {
				logger.debug("## Single Installment");
				logger.debug("## Value to add : {}", purchase.getValue());
				totalAmount = totalAmount.add(purchase.getValue());
			}
			invoice.setTotalAmount(totalAmount);
		}
    	logger.debug("## Total Amount : {}", totalAmount);
    	
    	if(totalAmount.compareTo(invoice.getTotalAmount()) != 0) {
			invoice.setTotalAmount(totalAmount);
			invoiceRepository.save(invoice);
			logger.debug("## Total Amount Updated! ");
		} else {
			logger.debug("## Total Amount is correct! ");
		}   	
    	return invoice;
    }
    
    // -------------------------------- GET OR CREATE CURRENT INVOICE BY PURCHASE DATE --------------------------
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
    
    // -------------------------------- FIND OR CREATE INVOICES BY CREDIT CARD DATE CONFIGURATIONS  ----------------------------------------------
    private Invoice findOrCreateInvoice(CreditCard card, LocalDate startDate, LocalDate endDate, LocalDate dueDate) {
        return invoiceRepository
                .findByCreditCardAndStartDateAndEndDate(card, startDate, endDate)
                .orElseGet(() -> {
                	
                	logger.debug("## ----------------------------- ## CREATE NEXT INVOICE ## ----------------------------- ## ");
                	// INVOICE 
                    Invoice invoice = new Invoice();
                    invoice.setCreditCard(card);
                    invoice.setStartDate(startDate);
                    invoice.setEndDate(endDate);
                    invoice.setDueDate(dueDate);
                    invoice.setStatus( endDate.isBefore(LocalDate.now()) ? InvoiceStatus.CLOSED : InvoiceStatus.PENDING);
                    invoice.setTotalAmount(BigDecimal.ZERO);      
                    
                    
                	// FIND SIGNATURES ON CREDIT CARD
                	if(creditPurchaseRepository.existsRepeatsOnLastInvoice(card.getCardId())) {
                		logger.debug("## Repeat exists on credit card");
                		List<CreditPurchase> repeatPurchases = creditPurchaseRepository.findRepeatPurchasesFromLastInvoice(card.getCardId());
                		invoice = invoiceRepository.save(invoice);
                		invoice.getPurchases().addAll(repeatPurchases);		
                		for (CreditPurchase creditPurchase : repeatPurchases) {
                			
                			logger.debug("## Repeat Purchase : " + creditPurchase.toString());
                			CreditPurchase newPurchase = creteRepeatedPurchase(creditPurchase,invoice);
                			invoice.setTotalAmount(invoice.getTotalAmount().add(newPurchase.getValue()));
	
						}
                		
                	}  else {
                		logger.debug("## There´s no purchases that must repeat");
                	}
                    return invoiceRepository.save(invoice);
                });
    }
    

    // --------------------------------  FIND OR CREATE FIRST OR SINGLE RETROATIVE INVOICE ( BY PURCHASE DATE ) -------------------------------- 
    private Invoice findOrCreateRetroativeInvoice(CreditCard card, CreditPurchaseForm purchaseForm) {
    	
    	logger.debug("## ----------------------------- ## CREATE RETROATIVE INVOICE ## ----------------------------- ## ");
    	LocalDate purchaseDate = purchaseForm.getPurchaseDateTime().toLocalDate();
    	logger.debug("## Purchase Date : {}",  purchaseDate);
    	
    	LocalDate dueDate = calculateDueDate(purchaseDate, card.getDueDate());
    	LocalDate startDate = calculateStartDate(purchaseDate, card.getBillingPeriodStart());
    	LocalDate endDate = calculateEndDate(purchaseDate, card.getBillingPeriodEnd());
        
    	logger.debug("## Due Date : {} - Start Date : {} - End Date : {}" ,  dueDate,startDate,endDate);
        return invoiceRepository
                .findByCreditCardAndStartDateAndEndDate(card, startDate, endDate)
                .orElseGet(() -> {	
                	// INVOICE 
                    Invoice invoice = new Invoice();
                    invoice.setCreditCard(card);
                    invoice.setStartDate(startDate);
                    invoice.setEndDate(endDate);
                    invoice.setDueDate(dueDate);
                    invoice.setStatus(InvoiceStatus.CLOSED);
                    invoice.setTotalAmount(BigDecimal.ZERO);     
                    return invoiceRepository.save(invoice);
                });
    }
    

    
    //  -------------------------------------------- INVOICEs DATE HANDLER -----------------------------------------------------
    
    private LocalDate calculateStartDate(LocalDate today, int startDate) {
        // e.g., start today or card's billing cycle
        return getValidDayOfMonth(today, startDate);
    }

    private LocalDate calculateEndDate(LocalDate today, int endDate) {
    	LocalDate invoiceEndDate = getValidDayOfMonth(today, endDate);
        // End date stays in the same month as start date
        return invoiceEndDate;
    }
    
    private LocalDate calculateDueDate(LocalDate today , int dueDate) {
    	LocalDate invoiceDueDate = getValidDayOfMonth(today, dueDate);
    	invoiceDueDate = invoiceDueDate.plusMonths(1);
        // example: end of month
        return invoiceDueDate;
    }

    private LocalDate getValidDayOfMonth(LocalDate date, int dayOfMonth) {
        int maxDayInMonth = date.lengthOfMonth();
        int validDay = Math.min(dayOfMonth, maxDayInMonth);
        return date.withDayOfMonth(validDay);
    }

    private boolean checkIfRetroative(LocalDate purchaseDate, CreditCard card) { 		
    	LocalDate currentInvoiceStartBilling = calculateStartDate(LocalDate.now(), card.getBillingPeriodEnd());	
    	return purchaseDate.isBefore(currentInvoiceStartBilling);
    }
}
