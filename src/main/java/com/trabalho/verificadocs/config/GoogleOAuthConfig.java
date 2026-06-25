package com.trabalho.verificadocs.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class GoogleOAuthConfig {

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    @ConditionalOnProperty(name = {"GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET"})
    ClientRegistrationRepository googleClientRegistrationRepository(Environment environment) {
        return new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE
                        .getBuilder("google")
                        .clientId(environment.getRequiredProperty("GOOGLE_CLIENT_ID"))
                        .clientSecret(environment.getRequiredProperty("GOOGLE_CLIENT_SECRET"))
                        .scope("openid", "email", "profile")
                        .build());
    }
}
