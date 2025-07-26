package com.thukera.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.thukera.security.service.UserPrinciple;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

	private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

	@Value("${my.finances.app.jwtSecret}")
	private String jwtSecret;

	@Value("${my.finances.app.jwtExpiration}")
	private int jwtExpiration;

	public String generateJwtToken(Authentication authentication) {

		UserPrinciple userPrincipal = (UserPrinciple) authentication.getPrincipal();
		Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

		String token = Jwts.builder().setSubject(userPrincipal.getUsername()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
				.signWith(key, SignatureAlgorithm.HS512).compact();
		return token;
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);

			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature -> Message: {} ", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token -> Message: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("Expired JWT token -> Message: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("Unsupported JWT token -> Message: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty -> Message: {}", e.getMessage());
		}

		return false;
	}

	public String getUserNameFromJwtToken(String token) {
		Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}
}