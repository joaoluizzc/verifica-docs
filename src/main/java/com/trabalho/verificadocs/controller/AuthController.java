package com.trabalho.verificadocs.controller;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trabalho.verificadocs.service.UsuarioService;

@Controller
public class AuthController {

    private final Environment environment;
    private final UsuarioService usuarioService;

    public AuthController(Environment environment, UsuarioService usuarioService) {
        this.environment = environment;
        this.usuarioService = usuarioService;
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

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("googleOAuthEnabled", googleOAuthConfigurado());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String criarConta(
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            @RequestParam("confirmarSenha") String confirmarSenha,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.criarConta(nome, email, senha, confirmarSenha);
            redirectAttributes.addFlashAttribute("sucesso", "Conta criada. Entre com seu e-mail e senha.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            redirectAttributes.addFlashAttribute("nome", nome);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/cadastro";
        }
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
