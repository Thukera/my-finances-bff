package com.thukera.user.controller;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thukera.config.security.jwt.JwtProvider;
import com.thukera.user.model.entities.User;
import com.thukera.user.model.forms.LoginForm;
import com.thukera.user.repository.RoleRepository;
import com.thukera.user.repository.UserRepository;


import jakarta.servlet.http.HttpServletResponse;
@RestController
@RequestMapping("/api/cookie")
public class CookieRestAPI {

	private static final Logger logger = LogManager.getLogger(CookieRestAPI.class);

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
	public ResponseEntity<?> authenticateUser(@RequestBody LoginForm loginRequest, HttpServletResponse response) {
		
		logger.debug("######## ### INSERT COOKIE ### ########");

	    Optional<User> usuario = loginRequest.getUsername().contains("@") ?
	            userRepository.findFirstByEmail(loginRequest.getUsername()) :
	            userRepository.findByUsername(loginRequest.getUsername());

	    if (usuario.isEmpty() || Boolean.FALSE.equals(usuario.get().getStatus())) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("message", "Invalid credentials or inactive user"));
	    }

	    Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(usuario.get().getUsername(), loginRequest.getPassword())
	    );

	    SecurityContextHolder.getContext().setAuthentication(authentication);

	    String accessToken = jwtProvider.generateTokenFromUsername(usuario.get().getUsername(), false);
	    String refreshToken = jwtProvider.generateTokenFromUsername(usuario.get().getUsername(), true);

	    // Access token cookie (HttpOnly)
//	    ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
//	            .httpOnly(true)
//	            .secure(true)
//	            .sameSite("Strict")
//	            .path("/")
//	            .maxAge(3600) // 1h
//	            .build();
	    
	    ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
	    	    .httpOnly(true)
	    	    .secure(false) // ⬅️ Disable HTTPS-only
	    	    .sameSite("Lax") // ⬅️ Allow cookies from your frontend on HTTP
	    	    .path("/")
	    	    .maxAge(3600)
	    	    .build();

	    
	    // Refresh token cookie (HttpOnly)
//	    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
//	            .httpOnly(true)
//	            .secure(true)
//	            .sameSite("Strict")
//	            .path("/api/cookie/refresh")
//	            .maxAge(7 * 24 * 3600) // 7 days
//	            .build();
	    
	    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
	    	    .httpOnly(true)
	    	    .secure(false)
	    	    .sameSite("Lax")
	    	    .path("/api/cookie/refresh")
	    	    .maxAge(7 * 24 * 3600)
	    	    .build();

	    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
	    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

	    // Only return user info, no tokens
	    return ResponseEntity.ok(Map.of(
	            "message", "Login successful",
	            "user", usuario.get().getUsername()
	    ));
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(
	        @CookieValue(value = "refresh_token", required = false) String refreshToken,
	        HttpServletResponse response) {

	    if (refreshToken == null || !jwtProvider.validateJwtToken(refreshToken)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("message", "Invalid refresh token"));
	    }

	    // Extract username from refresh token
	    String username = jwtProvider.getUserNameFromJwtToken(refreshToken);

	    // Generate new access token
	    String newAccessToken = jwtProvider.generateTokenFromUsername(username, false);

	    // Create HttpOnly cookie for new access token
	    ResponseCookie cookie = ResponseCookie.from("access_token", newAccessToken)
	            .httpOnly(true)
	            .secure(true)         // ⚠️ use true in production
	            .sameSite("Strict")   // "Lax" or "None" if cross-site
	            .path("/")
	            .maxAge(3600)         // 1h
	            .build();

	    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

	    // Return only a message, no token in body
	    return ResponseEntity.ok(Map.of("message", "Access token refreshed successfully"));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {

	    // Remove access token cookie
	    ResponseCookie clearAccessToken = ResponseCookie.from("access_token", "")
	            .httpOnly(true)
	            .secure(true)          // ⚠️ true in production
	            .sameSite("Strict")
	            .path("/")
	            .maxAge(0)             // expires immediately
	            .build();

	    // Remove refresh token cookie
	    ResponseCookie clearRefreshToken = ResponseCookie.from("refresh_token", "")
	            .httpOnly(true)
	            .secure(true)
	            .sameSite("Strict")
	            .path("/api/cookie/refresh")
	            .maxAge(0)
	            .build();

	    // Add cookies to response to clear them
	    response.addHeader(HttpHeaders.SET_COOKIE, clearAccessToken.toString());
	    response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshToken.toString());

	    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
	}

}
