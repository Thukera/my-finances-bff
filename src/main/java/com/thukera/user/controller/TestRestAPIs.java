package com.thukera.user.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thukera.user.model.forms.SignUpForm;

import jakarta.validation.Valid;

//@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class TestRestAPIs {
	
	private static final Logger logger = LogManager.getLogger(AuthRestAPIs.class);
	
	@GetMapping("/api/test/user")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public String userAccess() {
		return ">>> User Contents!";
	}

	@GetMapping("/api/test/pm")
	@PreAuthorize("hasRole('PM') or hasRole('ADMIN')")
	public String projectManagementAccess() {
		return ">>> Board Management Project";
	}
	
	@GetMapping("/api/test/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminAccess() {
		return ">>> Admin Contents";
	}
	
    @PostMapping("/api/test/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
    	logger.info("######## ### ######## ###  SIGN UP  ### ######## ### ########");
    	logger.info("######## ### Form : " + signUpRequest);
    	return ">>> post Admin";
    }
    
	@GetMapping("/api/test")
	public String runingTest() {
		return ">>> Runing";
	}
}