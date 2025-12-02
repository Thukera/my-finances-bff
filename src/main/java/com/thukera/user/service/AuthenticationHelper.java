package com.thukera.user.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.model.entities.User;
import com.thukera.user.repository.UserRepository;

/**
 * Helper service to handle authentication-related operations
 * Eliminates code duplication across controllers
 */
@Service
public class AuthenticationHelper {

    private static final Logger logger = LogManager.getLogger(AuthenticationHelper.class);
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Get the currently authenticated user from SecurityContext
     * @return User entity
     * @throws NotFoundException if user not found in database
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        logger.debug("### Fetching current user: {}", username);
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * Get the username of the currently authenticated user
     * @return username string
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * Check if the current user has ADMIN role
     * @return true if user is admin
     */
    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Get the current Authentication object
     * @return Authentication
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Check if current user has access to a resource owned by userId
     * Admins always have access
     * @param resourceOwnerId the owner's user ID
     * @return true if authorized
     */
    public boolean canAccessUserResource(Long resourceOwnerId) {
        if (isCurrentUserAdmin()) {
            return true;
        }
        User currentUser = getCurrentUser();
        return currentUser.getId().equals(resourceOwnerId);
    }
}
