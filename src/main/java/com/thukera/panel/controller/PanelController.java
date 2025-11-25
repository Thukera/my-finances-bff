package com.thukera.panel.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thukera.creditcard.repository.CreditcardRepository;
import com.thukera.creditcard.repository.InvoiceRepository;
import com.thukera.panel.dto.PanelCreditCardDTOFromUser;
import com.thukera.panel.dto.PanelUserDTO;
import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.model.entities.User;
import com.thukera.user.repository.UserRepository;

@RestController
@RequestMapping("/api/panel")
public class PanelController {
	
	private static final Logger logger = LogManager.getLogger(PanelController.class);

	//@Autowired
	//private CreditcardRepository creditcardRepository;
	
	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private UserRepository userRepository;
	
	@GetMapping
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> getHomePanel() {

		logger.debug("######## ### GET PANEL DETAILS BY TOKEN ### ########");

		try {
			// 1. Recover authenticated user from SecurityContext
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			logger.debug("### Auth : " + authentication);
			String username = authentication.getName(); // usually the "username" from your JWT
			logger.debug("### Username From Token : " + username);

			// 2. Fetch User from DB
			User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
			logger.debug("### Username From Entity : " + user.getUsername());
			
			PanelUserDTO panelUserDTO = PanelUserDTO.fromEntity(user);
			
			List<PanelCreditCardDTOFromUser> creditcards = panelUserDTO.getCreditcards();
			for (int i = 0; i < creditcards.size(); i++) {
				Long cardID = creditcards.get(i).getId();
				Optional<Long> invoiceId = (invoiceRepository.findTargetInvoiceId(cardID, LocalDate.now()));
				creditcards.get(i).setCurrentInvoice(invoiceId);		
			}
			return ResponseEntity.ok(panelUserDTO);

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
