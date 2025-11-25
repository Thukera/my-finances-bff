package com.thukera.user.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.thukera.config.security.jwt.JwtProvider;
import com.thukera.root.model.messages.JwtResponse;
import com.thukera.user.model.entities.Role;
import com.thukera.user.model.entities.User;
import com.thukera.user.model.enums.RoleName;
import com.thukera.user.model.forms.LoginForm;
import com.thukera.user.model.forms.SignUpForm;
import com.thukera.user.repository.RoleRepository;
import com.thukera.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthRestAPIs {

	private static final Logger logger = LogManager.getLogger(AuthRestAPIs.class);

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtProvider jwtProvider;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {

		logger.debug("######## ### ######## ###  SIGN IN  ### ######## ### ########");

		Optional<User> usuario;

		try {

			if (loginRequest.getUsername().contains("@")) {
				usuario = userRepository.findFirstByEmail(loginRequest.getUsername());
			} else {
				usuario = userRepository.findByUsername(loginRequest.getUsername());
			}

			if (usuario.get().getStatus().equals(true)) {
				Authentication authentication = authenticationManager
						.authenticate(new UsernamePasswordAuthenticationToken(usuario.get().getUsername(),
								loginRequest.getPassword()));

				SecurityContextHolder.getContext().setAuthentication(authentication);
				String jwt = jwtProvider.generateJwtToken(authentication);
				JwtResponse token = new JwtResponse(jwt);

				usuario.get().setToken(token);
				usuario.get().setPassword("");

				return ResponseEntity.ok(usuario.get().getToken());
			} else {
				logger.debug("## Inactive User");
				Map<String, String> body = new HashMap<>();
				body.put("message", "Inactive User - contact your administrator");
				return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
			}

		} catch (BadCredentialsException e) {
			logger.debug("##  Bad Credentials");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Bad Credentials");
			return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
		} catch (NoSuchElementException e) {
			logger.debug("##  Not Found");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "User Not Found - contact your administrator");
			return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/signup")
	//@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {

		logger.info("######## ### ######## ###  SIGN UP  ### ######## ### ########");

		try {

			if (userRepository.existsByUsername(signUpRequest.getUsername())) {
				logger.debug("### Username Exists!");
				Map<String, String> body = new HashMap<>();
				body.put("message", "Fail -> Username already in use!");
				return new ResponseEntity<>(body, HttpStatus.CONFLICT);
			}

			if (userRepository.existsByEmail(signUpRequest.getEmail())) {
				logger.debug("### Username Exists!");
				Map<String, String> body = new HashMap<>();
				body.put("message", "Fail -> Mail already in use!");
				return new ResponseEntity<>(body, HttpStatus.CONFLICT);
			}

			if (userRepository.existsByDoc(signUpRequest.getDoc())) {
				logger.debug("### Username Exists!");
				Map<String, String> body = new HashMap<>();
				body.put("message", "Fail -> DOC already in use!");
				return new ResponseEntity<>(body, HttpStatus.CONFLICT);
			}

			// Creating user's account
			User user = new User(signUpRequest.getDoc(), signUpRequest.getName(), signUpRequest.getUsername(),signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()), signUpRequest.getStatus());

			Set<String> strRoles = signUpRequest.getRole();
			Set<Role> roles = new HashSet<>();

			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Fail! -> Cause: ADMIN Role not found."));
					roles.add(adminRole);

					break;

				default:
					Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not found."));
					roles.add(userRole);
				}
			});

			user.setRoles(roles);
			userRepository.save(user);

			logger.debug("### User Saved!!");
			Map<String, String> body = new HashMap<>();
			body.put("message", "User Created!");

			return ResponseEntity.ok(body);

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
