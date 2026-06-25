package com.trabalho.verificadocs.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.trabalho.verificadocs.service.LogAcessoService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginAuditSuccessHandler implements AuthenticationSuccessHandler {

    private final LogAcessoService logAcessoService;

    public LoginAuditSuccessHandler(LogAcessoService logAcessoService) {
        this.logAcessoService = logAcessoService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        logAcessoService.registrar(authentication.getName(), true, request);
        response.sendRedirect(request.getContextPath() + "/documentos");
    }
}
