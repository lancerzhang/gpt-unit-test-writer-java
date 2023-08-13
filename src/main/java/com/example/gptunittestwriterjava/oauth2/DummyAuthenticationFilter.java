package com.example.gptunittestwriterjava.oauth2;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Collections;

public class DummyAuthenticationFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication dummyAuthentication = createDummyAuthentication();
        SecurityContextHolder.getContext().setAuthentication(dummyAuthentication);

        chain.doFilter(request, response);
    }

    private Authentication createDummyAuthentication() {
        PrincipalUser dummyPrincipal = new PrincipalUser(1, "01234567", "sampleUser", "sample.user@example.com");
        dummyPrincipal.setDelegators(Collections.singletonList(1));
        return new UsernamePasswordAuthenticationToken(dummyPrincipal, null, null);
    }
}
