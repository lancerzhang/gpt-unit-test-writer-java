package com.example.demo.oauth2;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoDefaultConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.JwtAccessTokenConverterConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

@Configuration
@EnableOAuth2Sso
@Profile({"dev", "prod"})
public class OAuth2SsoConfig extends OAuth2SsoDefaultConfiguration {

    public OAuth2SsoConfig(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        super.configure(http);
        http.headers().frameOptions().disable();
        http.csrf().disable();
        http.cors().disable();
    }

    @Override
    public void configure(final WebSecurity web) {
        web.ignoring().antMatchers("/.well-known/**");
    }

    @Bean
    public AccessTokenConverter accessTokenConverter(UserAuthenticationConverter userAuthenticationConverter) {
        DefaultAccessTokenConverter defaultAccessTokenConverter = new DefaultAccessTokenConverter();
        defaultAccessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
        return defaultAccessTokenConverter;
    }

    @Bean
    public UserAuthenticationConverter userAuthenticationConverter() {
        return new UserConverter();
    }

    @Bean
    public JwtAccessTokenConverterConfigurer jwtAccessTokenConverterConfigurer(AccessTokenConverter accessTokenConverter) {
        return converter -> converter.setAccessTokenConverter(accessTokenConverter);
    }
}
