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
import com.thukera.creditcard.service.CreditCardService;
import com.thukera.creditcard.service.CreditPurchaseService;
import com.thukera.creditcard.service.InvoiceManagementService;
import com.thukera.creditcard.service.InvoiceService;

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
	private InvoiceManagementService invoiceManagementService;
	
	@Autowired
	private InvoiceService invoiceService;


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



	// ======================================= INVOICE MANGEMENT  =======================================	
	
	@GetMapping("/invoice/{invoiceId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> getInvoiceDetails(@PathVariable Long invoiceId) {
		logger.debug("######## ### GET INVOICE BY ID: {} ### ########", invoiceId);	
		InvoiceDTO invoiceDTO = invoiceManagementService.getInvoiceById(invoiceId);
		return ResponseEntity.ok(invoiceDTO);
	}

	@PutMapping("/invoice/{invoiceId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> updateTotalAmount(@PathVariable Long invoiceId) {
		logger.debug("######## ### GET INVOICE BY ID: {} ### ########", invoiceId);
		InvoiceDTO invoiceDTO = invoiceManagementService.getInvoiceById(invoiceId);
		InvoiceDTO updatedInvoice = InvoiceDTO.fromEntity(invoiceService.updateTotalAmount(invoiceDTO));		
		return ResponseEntity.ok(updatedInvoice);
	}

	@GetMapping("/current-invoice/{cardid}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> getCurrentInvoice(@PathVariable("cardid") Long cardId) {
		logger.debug("######## ### GET CURRENT INVOICE FOR CARD ID: {} ### ########", cardId);

		InvoiceDTO invoiceDTO = invoiceManagementService.getCurrentInvoice(cardId);
		return ResponseEntity.ok(invoiceDTO);
	}

}
