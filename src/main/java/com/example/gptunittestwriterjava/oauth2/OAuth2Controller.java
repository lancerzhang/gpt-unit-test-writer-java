package com.example.gptunittestwriterjava.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth2")
public class OAuth2Controller {

    @GetMapping("/me")
    public PrincipalUser getPrincipalUser(Authentication authentication) {
        return (PrincipalUser) authentication.getPrincipal();
    }

}