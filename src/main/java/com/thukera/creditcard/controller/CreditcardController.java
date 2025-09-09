package com.thukera.creditcard.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thukera.creditcard.model.dto.InvoiceDTO;
import com.thukera.creditcard.model.dto.PurchaseDTO;
import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.entities.CreditPurchase;
import com.thukera.creditcard.model.entities.Invoice;
import com.thukera.creditcard.model.form.CreditCardForm;
import com.thukera.creditcard.model.form.CreditPurchaseForm;
import com.thukera.creditcard.repository.CreditcardRepository;
import com.thukera.creditcard.repository.InvoiceRepository;
import com.thukera.creditcard.repository.CreditPurchaseRepository;
import com.thukera.creditcard.service.InvoiceService;
import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.model.entities.User;
import com.thukera.user.repository.UserRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@RestController
@RequestMapping("/api/creditcard")
//@CrossOrigin(origins = "*", maxAge = 3600)
public class CreditcardController {

	private static final Logger logger = LogManager.getLogger(CreditcardController.class);

	@Autowired
	private CreditcardRepository creditcardRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CreditPurchaseRepository purchaseRepository;

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private InvoiceService invoiceService;

	@PostMapping
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> createCard(@RequestBody CreditCardForm creditcardForm) {

		logger.debug("######## ### CREATE CREDIT CARD ### ########");

		try {
			logger.debug("### CredicardForm : " + creditcardForm.toString());
			// 1. Recover authenticated user from SecurityContext
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			logger.debug("### Auth : " + authentication);
			String username = authentication.getName(); // usually the "username" from your JWT
			logger.debug("### Username From Token : " + username);

			// 2. Fetch User from DB
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new NotFoundException("User not found"));
			logger.debug("### Username From Entity : " + user.getUsername());

			// 3. Build CreditCard
			CreditCard card = new CreditCard();
			card.setUser(user);
			card.setBank(creditcardForm.getBank());
			card.setNickname(creditcardForm.getNickname());
			card.setEndnumbers(creditcardForm.getEndNumbers());
			card.setDueDate(creditcardForm.getDueDate());
			card.setBillingPeriodStart(creditcardForm.getBillingPeriodStart());
			card.setBillingPeriodEnd(creditcardForm.getBillingPeriodEnd());
			card.setTotalLimit(creditcardForm.getTotalLimit());

			logger.debug("### Card : " + card.toString());

			// 4. Save
			CreditCard saved = creditcardRepository.save(card);

			CreditCardForm savedForm = new CreditCardForm().fromModel(saved);

			return ResponseEntity.ok(savedForm);

		} catch (TransactionSystemException e) {
			Throwable root = e.getRootCause();
			logger.error("### Root cause: {}", root != null ? root.getMessage() : e.getMessage(), e);
			Map<String, String> body = new HashMap<>();
			body.put("message", "Validation / transaction error");
			return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

		} catch (NotFoundException e) {
			logger.debug("### NotFoundException Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Não encontrado");
			return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);

		} catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> getCardDetails(@PathVariable Long id) {

		logger.debug("######## ### GET CREDIT CARD BY ID ### ########");

		try {
			logger.debug("### Credicard ID : " + id);
			// 1. Recover authenticated user from SecurityContext
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			logger.debug("### Auth : " + authentication);
			String username = authentication.getName(); // usually the "username" from your JWT
			logger.debug("### Username From Token : " + username);

			// 2. Fetch User from DB
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new NotFoundException("User not found"));
			logger.debug("### Username From Entity : " + user.getUsername());

			// 3. Build CreditCard
			CreditCard card = creditcardRepository.findById(id)
					.orElseThrow(() -> new NotFoundException("Cartão não encontrado"));
			;

			// 4. Check if ADMIN ROLE
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

			if (user.getId() != card.getUser().getId() && !isAdmin) {
				logger.debug("### Card : " + card.toString());
				Map<String, String> body = new HashMap<>();
				body.put("message", "Cartão Não Pertece ao usuario");
				return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
			} else {
				CreditCardForm savedForm = new CreditCardForm().fromModel(card);

				return ResponseEntity.ok(savedForm);
			}

		} catch (TransactionSystemException e) {
			Throwable root = e.getRootCause();
			logger.error("### Root cause: {}", root != null ? root.getMessage() : e.getMessage(), e);
			Map<String, String> body = new HashMap<>();
			body.put("message", "Validation / transaction error");
			return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

		} catch (NotFoundException e) {
			logger.debug("### NotFoundException Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Não encontrado");
			return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);

		} catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/purchase")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> insertPurchase(@RequestBody CreditPurchaseForm purchaseForm) {

		logger.debug("######## ### INSERT PURCHASE ### ########");

		try {

			// Form and User Validation
			logger.debug("### Purchase Form : " + purchaseForm.toString());
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			logger.debug("### Auth : " + authentication);
			String username = authentication.getName();
			logger.debug("### Username From Token : " + username);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new NotFoundException("User not found"));
			logger.debug("### Username From Entity : " + user.getUsername());
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

			// Credit Card and Invoice Adjustment
			CreditCard creditcard = creditcardRepository.findById(purchaseForm.getCreditCardId())
					.orElseThrow(() -> new NotFoundException("Cartão não encontrado"));
			logger.debug("### Creditcard: {}", creditcard.toString());

			boolean isCardFromUser = (user.getId() == creditcard.getUser().getId());

			if (!isAdmin && !isCardFromUser) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Não autorizado"));
			}

			CreditPurchase newPurchase = invoiceService.createPurchase(purchaseForm, creditcard);
			logger.debug("## NEW CREDIT PURCHASE : {}", newPurchase);

			creditcard.setUsedLimit(creditcard.getUsedLimit().add(newPurchase.getValue()));
			creditcardRepository.save(creditcard);

			PurchaseDTO purchaseDTO = PurchaseDTO.fromEntity(newPurchase);
			return ResponseEntity.ok(purchaseDTO);

		} catch (TransactionSystemException e) {
			Throwable root = e.getRootCause();
			logger.error("### Root cause: {}", root != null ? root.getMessage() : e.getMessage(), e);
			Map<String, String> body = new HashMap<>();
			body.put("message", "Validation / transaction error");
			return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

		} catch (NotFoundException e) {
			logger.debug("### NotFoundException Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Não encontrado");
			return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);

		} catch (Exception e) {
			logger.error("### Error inserting purchase", e);
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/purchase/{purchaseId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> gettPurchaseDetails(@PathVariable Long purchaseId) {

		logger.debug("######## ### GET PURCHASE BY ID ### ########");

		try {

			logger.debug("### Purchase ID : " + purchaseId);

			// 1. Recover authenticated user from SecurityContext
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			logger.debug("### Auth : " + authentication);
			String username = authentication.getName();
			logger.debug("### Username From Token : " + username);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new NotFoundException("User not found"));
			logger.debug("### Username From Entity : " + user.getUsername());
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

			CreditPurchase purchase = purchaseRepository.findById(purchaseId)
					.orElseThrow(() -> new NotFoundException("Purchase not found"));

			// safely get the first invoice (if any)
			Invoice invoice = purchase.getInvoices().stream().findFirst()
					.orElseThrow(() -> new NotFoundException("No invoice linked to this purchase"));

			// validate the user
			boolean isPurchaseFromUser = user.getId().equals(invoice.getCreditCard().getUser().getId());

			if (isAdmin || isPurchaseFromUser) {
				logger.debug("### Permitions OK");
				return ResponseEntity.ok(PurchaseDTO.fromEntity(purchase));
			} else {
				Map<String, String> body = new HashMap<>();
				body.put("message", "Não autorizado");
				return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
			}

		} catch (TransactionSystemException e) {
			Throwable root = e.getRootCause();
			logger.error("### Root cause: {}", root != null ? root.getMessage() : e.getMessage(), e);
			Map<String, String> body = new HashMap<>();
			body.put("message", "Validation / transaction error");
			return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

		} catch (NotFoundException e) {
			logger.debug("### NotFoundException Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Não encontrado");
			return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);

		} catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/invoice/{invoiceId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> gettInvoiceDetails(@PathVariable Long invoiceId) {

		logger.debug("######## ### GET INVOICE BY ID ### ########");

		try {

			logger.debug("### Invoice ID : " + invoiceId);

			// 1. Recover authenticated user from SecurityContext
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			logger.debug("### Auth : " + authentication);
			String username = authentication.getName(); // usually the "username" from your JWT
			logger.debug("### Username From Token : " + username);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new NotFoundException("User not found"));
			logger.debug("### Username From Entity : " + user.getUsername());
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

			Invoice invoice = invoiceRepository.findById(invoiceId)
					.orElseThrow(() -> new NotFoundException("Invoice not found"));
			;

			boolean isInvoiceFromUser = (user.getId() == invoice.getCreditCard().getUser().getId());

			if (isAdmin || isInvoiceFromUser) {
				logger.debug("### Permitions OK");

				InvoiceDTO invoiceDTO = InvoiceDTO.fromEntity(invoice);

				return ResponseEntity.ok(invoiceDTO);
			} else {
				Map<String, String> body = new HashMap<>();
				body.put("message", "Não autorizado");
				return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
			}

		} catch (TransactionSystemException e) {
			Throwable root = e.getRootCause();
			logger.error("### Root cause: {}", root != null ? root.getMessage() : e.getMessage(), e);
			Map<String, String> body = new HashMap<>();
			body.put("message", "Validation / transaction error");
			return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

		} catch (NotFoundException e) {
			logger.debug("### NotFoundException Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Não encontrado");
			return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);

		} catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
