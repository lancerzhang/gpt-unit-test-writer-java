package com.example.gptunittestwriterjava.oauth2;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

public class PrincipalValidator {
    public static void validateUserPermission(Integer userId, Authentication authentication) {
        PrincipalUser authenticatedUser = (PrincipalUser) authentication.getPrincipal();
        if (!authenticatedUser.getDelegators().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
        }
    }
}
