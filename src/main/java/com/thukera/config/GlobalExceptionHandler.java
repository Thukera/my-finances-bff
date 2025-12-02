package com.thukera.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.controller.AuthRestAPIs;

import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.JwtException;

import jakarta.validation.ConstraintViolationException;

import java.util.*;

/**
 * Global exception handler for the entire application
 * Centralizes error handling and eliminates duplicate try-catch blocks in controllers
 */
@RestControllerAdvice(basePackages = "com.thukera")
public class GlobalExceptionHandler {

	private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

	// 1. Validation errors (e.g., @Valid + @RequestBody)
	// Handle validation errors
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException ex) {

		logger.error("### GLOBAL EXCEPTION HANDLER ");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}

	// 2. @Validated on request parameters
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {

		logger.error("### GLOBAL EXCEPTION HANDLER ");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		List<String> errors = ex.getConstraintViolations().stream()
				.map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("message", "Constraint violation", "errors", errors));
	}

	// 3. Missing required parameters
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleMissingParams(MissingServletRequestParameterException ex) {

		logger.error("### GLOBAL EXCEPTION HANDLER ");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		String error = ex.getParameterName() + " parameter is missing";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", error));
	}

	// 4. Type mismatch (e.g., expected int but got string)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

		logger.error("### GLOBAL EXCEPTION HANDLER ");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		String error = ex.getName() + " should be of type " + ex.getRequiredType().getSimpleName();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", error));
	}

	// 5. Illegal arguments (custom logic issues)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {

		logger.error("### GLOBAL EXCEPTION HANDLER ");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
	}

	// 6. JWT parsing/authentication errors
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<?> handleJwtErrors(JwtException ex) {

		logger.error("### GLOBAL EXCEPTION HANDLER ");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("message", "Invalid or expired JWT token", "error", ex.getMessage()));
	}

	// 7. JWT AuthorizationDeniedException
	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<?> authorizationJwtErrors(JwtException ex) {

		logger.error("### GLOBAL EXCEPTION HANDLER ");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		Map<String, String> body = new HashMap<>();
		body.put("message", "Authorization Denied");
		body.put("error", ex.getMessage());

		return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);

	}

	// 8. JSON parse / bad request body
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleJsonParseErrors(HttpMessageNotReadableException ex) {
		logger.error("### GLOBAL EXCEPTION HANDLER - JSON Parse Error");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		Map<String, String> body = new HashMap<>();
		body.put("message", "Invalid request body format");
		body.put("error", ex.getMostSpecificCause().getMessage());

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	// 9. NotFoundException - custom exception for resource not found
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
		logger.error("### GLOBAL EXCEPTION HANDLER - NotFoundException");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage());

		Map<String, String> body = new HashMap<>();
		body.put("message", ex.getMessage() != null ? ex.getMessage() : "Resource not found");

		return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	}

	// 10. SecurityException - unauthorized access attempts
	@ExceptionHandler(SecurityException.class)
	public ResponseEntity<?> handleSecurityException(SecurityException ex) {
		logger.warn("### GLOBAL EXCEPTION HANDLER - SecurityException");
		logger.warn("### Exception : " + ex.getClass());
		logger.warn("### Message : " + ex.getMessage());

		Map<String, String> body = new HashMap<>();
		body.put("message", ex.getMessage() != null ? ex.getMessage() : "Access denied");

		return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
	}

	// 11. TransactionSystemException - database/validation errors
	@ExceptionHandler(TransactionSystemException.class)
	public ResponseEntity<?> handleTransactionException(TransactionSystemException ex) {
		logger.error("### GLOBAL EXCEPTION HANDLER - TransactionSystemException");
		Throwable root = ex.getRootCause();
		logger.error("### Root cause: {}", root != null ? root.getMessage() : ex.getMessage(), ex);

		Map<String, String> body = new HashMap<>();
		body.put("message", "Validation or transaction error");
		if (root != null) {
			body.put("error", root.getMessage());
		}

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	// 12. Catch-all for unexpected exceptions
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGenericException(Exception ex) {
		logger.error("### GLOBAL EXCEPTION HANDLER - Unexpected Exception");
		logger.error("### Exception : " + ex.getClass());
		logger.error("### Message : " + ex.getMessage(), ex);

		Map<String, String> body = new HashMap<>();
		body.put("message", "Internal server error");
		body.put("error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");

		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
