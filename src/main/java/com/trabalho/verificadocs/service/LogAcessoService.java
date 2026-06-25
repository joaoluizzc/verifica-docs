package com.trabalho.verificadocs.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trabalho.verificadocs.model.LogAcesso;
import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.repository.LogAcessoRepository;
import com.trabalho.verificadocs.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class LogAcessoService {

    private final LogAcessoRepository logAcessoRepository;
    private final UsuarioRepository usuarioRepository;

    public LogAcessoService(LogAcessoRepository logAcessoRepository, UsuarioRepository usuarioRepository) {
        this.logAcessoRepository = logAcessoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(String emailInformado, boolean sucesso, HttpServletRequest request) {
        String email = emailInformado == null || emailInformado.isBlank() ? "nao informado" : emailInformado;
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

        if (sucesso && usuario != null) {
            usuario.setUltimoLoginEm(LocalDateTime.now());
        }

        LogAcesso log = new LogAcesso();
        log.setEmailInformado(email);
        log.setSucesso(sucesso);
        log.setIp(extrairIp(request));
        log.setUserAgent(limitar(request.getHeader("User-Agent"), 255));
        log.setUsuario(usuario);
        log.registrarTentativa();
        logAcessoRepository.save(log);
    }

    private String extrairIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return limitar(forwarded.split(",")[0].trim(), 80);
        }
        return limitar(request.getRemoteAddr(), 80);
    }

    private String limitar(String valor, int limite) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= limite ? valor : valor.substring(0, limite);
    }
}
