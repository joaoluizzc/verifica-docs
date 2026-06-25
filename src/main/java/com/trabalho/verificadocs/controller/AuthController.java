package com.trabalho.verificadocs.controller;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    private final Environment environment;

    public AuthController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/documentos";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("googleOAuthEnabled", googleOAuthConfigurado());
        return "login";
    }

    private boolean googleOAuthConfigurado() {
        return possuiValor("GOOGLE_CLIENT_ID")
                && possuiValor("GOOGLE_CLIENT_SECRET")
                || possuiValor("spring.security.oauth2.client.registration.google.client-id")
                && possuiValor("spring.security.oauth2.client.registration.google.client-secret");
    }

    private boolean possuiValor(String propriedade) {
        String valor = environment.getProperty(propriedade);
        return valor != null && !valor.isBlank();
    }
}
