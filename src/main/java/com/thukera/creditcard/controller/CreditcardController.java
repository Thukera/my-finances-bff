package com.thukera.creditcard.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.thukera.creditcard.model.dto.InvoiceDTO;
import com.thukera.creditcard.model.dto.PurchaseDTO;
import com.thukera.creditcard.model.form.CreditCardForm;
import com.thukera.creditcard.model.form.CreditPurchaseForm;
import com.thukera.creditcard.service.CreditCardService;
import com.thukera.creditcard.service.CreditPurchaseService;
import com.thukera.creditcard.service.InvoiceManagementService;

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


	/**
	 * Create a new credit card for the authenticated user
	 * @param creditcardForm the credit card data
	 * @return ResponseEntity with created card or error
	 */
	@PostMapping
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<CreditCardForm> createCard(@RequestBody CreditCardForm creditcardForm) {
		logger.debug("######## ### CREATE CREDIT CARD ### ########");
		logger.debug("### CreditCardForm: {}", creditcardForm);

		CreditCardForm savedCard = creditCardService.createCreditCard(creditcardForm);
		return ResponseEntity.ok(savedCard);
	}


	/**
	 * Get credit card details by ID
	 * @param id the card ID
	 * @return ResponseEntity with card details or error
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<CreditCardForm> getCardDetails(@PathVariable Long id) {
		logger.debug("######## ### GET CREDIT CARD BY ID: {} ### ########", id);

		CreditCardForm cardForm = creditCardService.getCreditCardById(id);
		return ResponseEntity.ok(cardForm);
	}


	/**
	 * Create a new purchase on a credit card
	 * @param purchaseForm the purchase data
	 * @return ResponseEntity with created purchase or error
	 */
	@PostMapping("/purchase")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<PurchaseDTO> insertPurchase(@RequestBody CreditPurchaseForm purchaseForm) {
		logger.debug("######## ### INSERT PURCHASE ### ########");
		logger.debug("### PurchaseForm: {}", purchaseForm);

		PurchaseDTO purchaseDTO = creditPurchaseService.createPurchase(purchaseForm);
		return ResponseEntity.ok(purchaseDTO);
	}


	/**
	 * Get purchase details by ID
	 * @param purchaseId the purchase ID
	 * @return ResponseEntity with purchase details or error
	 */
	@GetMapping("/purchase/{purchaseId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<PurchaseDTO> getPurchaseDetails(@PathVariable Long purchaseId) {
		logger.debug("######## ### GET PURCHASE BY ID: {} ### ########", purchaseId);

		PurchaseDTO purchaseDTO = creditPurchaseService.getPurchaseById(purchaseId);
		return ResponseEntity.ok(purchaseDTO);
	}


	/**
	 * Get invoice details by ID
	 * @param invoiceId the invoice ID
	 * @return ResponseEntity with invoice details or error
	 */
	@GetMapping("/invoice/{invoiceId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> getInvoiceDetails(@PathVariable Long invoiceId) {
		logger.debug("######## ### GET INVOICE BY ID: {} ### ########", invoiceId);

		InvoiceDTO invoiceDTO = invoiceManagementService.getInvoiceById(invoiceId);
		return ResponseEntity.ok(invoiceDTO);
	}

	/**
	 * Get current invoice for a credit card
	 * @param cardId the card ID
	 * @return ResponseEntity with current invoice or error
	 */
	@GetMapping("/current-invoice/{cardid}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<InvoiceDTO> getCurrentInvoice(@PathVariable("cardid") Long cardId) {
		logger.debug("######## ### GET CURRENT INVOICE FOR CARD ID: {} ### ########", cardId);

		InvoiceDTO invoiceDTO = invoiceManagementService.getCurrentInvoice(cardId);
		return ResponseEntity.ok(invoiceDTO);
	}

}
