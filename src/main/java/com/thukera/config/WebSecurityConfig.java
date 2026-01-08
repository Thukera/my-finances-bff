package com.thukera.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.thukera.config.security.jwt.JwtAuthEntryPoint;
import com.thukera.config.security.jwt.JwtAuthTokenFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig implements WebMvcConfigurer {

	@Autowired
	private JwtAuthEntryPoint unauthorizedHandler;

	@Autowired
	private UserDetailsService userDetailsService;

	// Public endpoints that don't require authentication
	private static final String[] AUTH_WHITELIST = { 
			// Swagger/OpenAPI
			"/swagger-resources/**", 
			"/webjars/**", 
			"/v3/api-docs/**",
			"/swagger-ui/**", 
			"/swagger-ui.html", 
			"/swagger-ui/index.html",
			
			// Static resources
			"/uploads/**",
			
			// Public API endpoints
			"/api/auth/**",      // Authentication endpoints (login, register, etc.)
			"/api/cookie/**",    // Cookie-based authentication
			"/api/test"          // Only the base test endpoint (not /api/test/**)
	};

	/**
	 * Completely bypass Spring Security for static resources
	 * This tells Spring Security to ignore these paths entirely
	 */
	@Bean
	public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()
			.requestMatchers("/uploads/**", "/favicon.ico", "/error");
	}

	@Bean
	public JwtAuthTokenFilter authenticationJwtTokenFilter() {
		return new JwtAuthTokenFilter();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	        .csrf(csrf -> csrf.disable())
	        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/uploads/**").permitAll()
	            .requestMatchers(AUTH_WHITELIST).permitAll()
	            .anyRequest().authenticated()
	        );

	    http.authenticationProvider(authenticationProvider());
	    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
//		configuration.setAllowedOriginPatterns(Arrays.asList(
//		        "http://localhost:3000",
//		        "http://192.168.0.60:3000"
//		    ));
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
		configuration.setAllowCredentials(true);
		configuration.addAllowedHeader("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    registry.addResourceHandler("/uploads/**")
	            .addResourceLocations("file:./uploads/"); // Use relative path for Docker
	}
}
