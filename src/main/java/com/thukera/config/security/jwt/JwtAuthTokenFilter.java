package com.thukera.config.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import com.thukera.user.security.service.UserDetailsServiceImpl;

public class JwtAuthTokenFilter extends OncePerRequestFilter {
	@Autowired
	private JwtProvider tokenProvider;
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

	// Public endpoints that should skip JWT authentication
	private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
		"/uploads/**",
		"/swagger-resources/**",
		"/webjars/**",
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/swagger-ui/index.html",
		"/api/auth/**",
		"/api/cookie/**",
		"/api/test"  // Only base endpoint, not /** (protected endpoints need auth)
	);

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * Skip JWT filter for public endpoints
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		logger.debug("### Checking if path should skip JWT filter: {}", path);
		
		boolean shouldSkip = PUBLIC_ENDPOINTS.stream()
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
		
		if (shouldSkip) {
			logger.debug("### Skipping JWT authentication for public path: {}", path);
		}
		
		return shouldSkip;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String jwt = resolveToken(request);
			if (jwt != null && tokenProvider.validateJwtToken(jwt)) {
				String username = tokenProvider.getUserNameFromJwtToken(jwt);
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			logger.debug("Cannot set user authentication -> Message: {}", e.getMessage());
		}
		filterChain.doFilter(request, response);
	}

	/**
	 * Resolves JWT token either from Authorization header or cookie
	 */
	private String resolveToken(HttpServletRequest request) {
		// 1. Authorization header
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		// 2. Cookie fallback
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName()) && cookie.getValue() != null
						&& !cookie.getValue().isEmpty()) {
					return cookie.getValue();
				}
			}
		}
		return null; // no token found
	}
}