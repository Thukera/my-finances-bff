package com.thukera.creditcard.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.creditcard.model.form.CreditCardForm;
import com.thukera.creditcard.repository.CreditcardRepository;
import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.controller.AuthRestAPIs;
import com.thukera.user.model.entities.User;
import com.thukera.user.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController("api/creditcard")
public class CreditcardController {

	private static final Logger logger = LogManager.getLogger(CreditcardController.class);

	@Autowired
	private CreditcardRepository creditcardRepository;

	@Autowired
	private UserRepository userRepository;

	@PostMapping
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> createCard(@RequestBody CreditCardForm creditcardForm) {

		logger.debug("######## ### CREATE CREDIT CARD ### ########");

		try {

			  // 1. Recover authenticated user from SecurityContext
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        logger.debug("### Auth : " + authentication);
	        String username = authentication.getName(); // usually the "username" from your JWT
	        logger.debug("### Username From Token : " + username);

	        // 2. Fetch User from DB
	        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
	        logger.debug("### Username From Entity : " + user.getUsername());
	        
	        // 3. Build CreditCard
	        CreditCard card = new CreditCard();
	        card.setUser(user);
	        card.setBank(creditcardForm.getBank());
	        card.setEndnumbers(creditcardForm.getEndNumbers());
	        card.setBillingPeriodStart(creditcardForm.getBillingInvoiceStart());
	        card.setBillingPeriodEnd(creditcardForm.getBillingInvoiceEnd()-1);
	        card.setUsedLimit(BigDecimal.ZERO);
	        card.setTotalLimit(creditcardForm.getTotalLimit());

	        // 4. Save
	        CreditCard saved = creditcardRepository.save(card);

	        return ResponseEntity.ok(saved);

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
