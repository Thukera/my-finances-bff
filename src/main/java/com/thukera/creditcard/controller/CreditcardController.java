package com.thukera.creditcard.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.thukera.creditcard.model.dto.InvoiceDTO;
import com.thukera.creditcard.model.dto.PurchaseDTO;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.form.CreditCardForm;
import com.thukera.creditcard.model.form.CreditPurchaseForm;
import com.thukera.creditcard.model.form.InvoiceForm;
import com.thukera.creditcard.service.CreditCardService;
import com.thukera.creditcard.service.CreditPurchaseService;
import com.thukera.creditcard.service.InvoiceService;
import com.thukera.creditcard.service.CreditTransactionService;

/**
 * REST Controller for Credit Card operations
 * Thin controller following best practices - delegates all business logic to services
 */

// GIT
@RestController
@RequestMapping("/api/creditcard")
public class CreditcardController {

	private static final Logger logger = LogManager.getLogger(CreditcardController.class);

	@Autowired
	private CreditCardService creditCardService;

	@Autowired
	private CreditPurchaseService creditPurchaseService;

	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private CreditTransactionService creditTransactionService;


	// ======================================= CREDIT CARD CRUD =======================================
	@PostMapping
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<CreditCardForm> createCard(@RequestBody CreditCardForm creditcardForm) {
		logger.debug("######## ### CREATE CREDIT CARD ### ########");
		logger.debug("### CreditCardForm: {}", creditcardForm);

		CreditCardForm savedCard = creditCardService.createCreditCard(creditcardForm);
		return ResponseEntity.ok(savedCard);
	}


	@GetMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<CreditCardForm> getCardDetails(@PathVariable Long id) {
		logger.debug("######## ### GET CREDIT CARD BY ID: {} ### ########", id);

		CreditCardForm cardForm = creditCardService.getCreditCardById(id);
		return ResponseEntity.ok(cardForm);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<CreditCardForm> putCardDetails(@PathVariable Long id, @RequestBody CreditCardForm creditcardForm) {
		logger.debug("######## ### UPDATE CREDIT CARD  | ID: {} ### ########", id);
		logger.debug("### CreditCardForm: {}", creditcardForm);

		CreditCard existingCard = creditCardService.getCreditCardEntityById(id);
		CreditCardForm cardForm = creditCardService.updateCreditCard(existingCard, creditcardForm);
		
		return ResponseEntity.ok(cardForm);
	}
	
	@PutMapping("/check-limit/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<CreditCardForm> putCardUsedLimit(@PathVariable Long id) {
		logger.debug("######## ### UPDATE CREDIT CARD  | ID: {} ### ########", id);

		CreditCard existingCard = creditCardService.getCreditCardEntityById(id);
		CreditCardForm cardForm = creditCardService.updateCreditCardUsedLimit(existingCard);
		
		return ResponseEntity.ok(cardForm);
	}


		
	// ======================================= CREDIT PURCHASE CRUD =======================================
	
	@PostMapping("/purchase")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<PurchaseDTO> insertPurchase(@RequestBody CreditPurchaseForm purchaseForm) {
		logger.debug("######## ### INSERT PURCHASE ### ########");
		logger.debug("### PurchaseForm: {}", purchaseForm);

		PurchaseDTO purchaseDTO = creditPurchaseService.createPurchase(purchaseForm);
		return ResponseEntity.ok(purchaseDTO);
	}


	@GetMapping("/purchase/{purchaseId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<PurchaseDTO> getPurchaseDetails(@PathVariable Long purchaseId) {
		logger.debug("######## ### GET PURCHASE BY ID: {} ### ########", purchaseId);

		PurchaseDTO purchaseDTO = creditPurchaseService.getPurchaseById(purchaseId);
		return ResponseEntity.ok(purchaseDTO);
	}
	
	@PutMapping("/purchase/update/{purchaseId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<PurchaseDTO> updatePurchaseDetails(@PathVariable Long purchaseId, @RequestBody CreditPurchaseForm purchaseForm) {
		logger.debug("######## ### UPDATE PURCHASE WITH ID: {} ### ########", purchaseId);

		PurchaseDTO purchaseDTO = creditPurchaseService.updatePurchaseById(purchaseId,purchaseForm);
		return ResponseEntity.ok(purchaseDTO);
	}
	
	@PostMapping("/purchase/delete/{purchaseId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<PurchaseDTO> deletePurchase(@PathVariable Long purchaseId) {
		logger.debug("######## ### DELETE PURCHASE WITH ID: {} ### ########", purchaseId);

		creditPurchaseService.deletePurchase(purchaseId);
		return ResponseEntity.ok().build();
	}



	// ======================================= INVOICE MANGEMENT  =======================================	
	
	@GetMapping("/invoice/{invoiceId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> getInvoiceDetails(@PathVariable Long invoiceId) {
		logger.debug("######## ### GET INVOICE BY ID: {} ### ########", invoiceId);	
		InvoiceDTO invoiceDTO = invoiceService.getInvoiceById(invoiceId);
		return ResponseEntity.ok(invoiceDTO);
	}
	
	@PutMapping("/invoice/{invoiceId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> getCurrentInvoice(@PathVariable("invoiceId") Long invoiceId, @RequestBody InvoiceForm invoiceForm) {
		logger.debug("######## ### UPDATE INVOICE : {} ### ########", invoiceId);	
		
		InvoiceDTO invoiceDTO = invoiceService.putInvoice(invoiceId, invoiceForm);
		return ResponseEntity.ok(invoiceDTO);
	}

	@PutMapping("/invoice/check-amount/{invoiceId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> updateTotalAmount(@PathVariable Long invoiceId) {
		logger.debug("######## ### CHECK INVOICE TOTAL AMOUNT BY ID: {} ### ########", invoiceId);
		InvoiceDTO invoiceDTO = invoiceService.getInvoiceById(invoiceId);
		InvoiceDTO updatedInvoice = InvoiceDTO.fromEntity(creditTransactionService.updateTotalAmount(invoiceDTO));		
		return ResponseEntity.ok(updatedInvoice);
	}
	
	@PutMapping("/invoice/change-status/{invoiceId}/{status}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> updateInvoiceStatus(@PathVariable("invoiceId") Long invoiceId, @PathVariable("status") String status) {
		logger.debug("######## ### UPDATE INVOICE STATUS: {} ### ########", invoiceId);		
		InvoiceDTO invoiceDTO = invoiceService.putInvoiceStatus(invoiceId,status);
		return ResponseEntity.ok(invoiceDTO);
	}

	@GetMapping("/invoice/current-invoice/{invoiceId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> getCurrentInvoice(@PathVariable("invoiceId") Long cardId) {
		logger.debug("######## ### GET CURRENT INVOICE FOR CARD ID: {} ### ########", cardId);		
		InvoiceDTO invoiceDTO = invoiceService.getCurrentInvoice(cardId);
		return ResponseEntity.ok(invoiceDTO);
	}

}
