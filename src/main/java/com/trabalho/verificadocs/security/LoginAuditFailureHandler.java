package com.trabalho.verificadocs.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.trabalho.verificadocs.service.LogAcessoService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginAuditFailureHandler implements AuthenticationFailureHandler {

    private final LogAcessoService logAcessoService;

    public LoginAuditFailureHandler(LogAcessoService logAcessoService) {
        this.logAcessoService = logAcessoService;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        logAcessoService.registrar(request.getParameter("email"), false, request);
        response.sendRedirect(request.getContextPath() + "/login?erro");
    }
}
