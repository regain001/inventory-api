package com.ecom.inventory.security;


import com.ecom.inventory.entity.AppUser;
import com.ecom.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 *
 * @author SohanBappy
 */
@Component
public class AuthenticatedUser {

    private static UserService userService;

    @Autowired
    public AuthenticatedUser(UserService userService) {
        this.userService = userService;
    }

    public static String getAuthenticatedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public static Object getAuthenticatedUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getPrincipal();
    }

    public static AppUser getAuthenticatedUser() {
        return Optional.ofNullable(userService.getUserByUserName(getAuthenticatedUserName()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
