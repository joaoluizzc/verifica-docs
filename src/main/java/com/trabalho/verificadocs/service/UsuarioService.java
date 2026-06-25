package com.trabalho.verificadocs.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trabalho.verificadocs.model.Perfil;
import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.repository.PerfilRepository;
import com.trabalho.verificadocs.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuarioAutenticado(Authentication authentication) {
        return usuarioRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado"));
    }

    @Transactional
    public Usuario criarConta(String nome, String email, String senha, String confirmarSenha) {
        validarCadastro(nome, email, senha, confirmarSenha);

        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Ja existe uma conta com este e-mail.");
        }

        Perfil perfil = perfilRepository.findByNome("USUARIO").orElseGet(() -> {
            Perfil novoPerfil = new Perfil();
            novoPerfil.setNome("USUARIO");
            novoPerfil.setDescricao("Usuario autorizado a enviar e consultar analises de documentos");
            return perfilRepository.save(novoPerfil);
        });

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim().toLowerCase());
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    private void validarCadastro(String nome, String email, String senha, String confirmarSenha) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe seu nome.");
        }

        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Informe um e-mail valido.");
        }

        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }

        if (!senha.equals(confirmarSenha)) {
            throw new IllegalArgumentException("As senhas nao conferem.");
        }
    }
}
