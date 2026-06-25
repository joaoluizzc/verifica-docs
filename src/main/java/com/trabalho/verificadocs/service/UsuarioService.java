package com.trabalho.verificadocs.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @Transactional
    public Usuario buscarUsuarioAutenticado(Authentication authentication) {
        String email = extrairEmail(authentication);
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> criarUsuarioAutenticadoGoogle(authentication, email));
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

    @Transactional
    public Usuario buscarOuCriarUsuarioExterno(String nome, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail nao retornado pelo provedor de login.");
        }

        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> salvarUsuarioExterno(nome, email));
    }

    private Usuario criarUsuarioAutenticadoGoogle(Authentication authentication, String email) {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String nome = atributo(oauth2User, "name");
            return salvarUsuarioExterno(nome, email);
        }

        throw new IllegalStateException("Usuario autenticado nao encontrado");
    }

    private Usuario salvarUsuarioExterno(String nome, String email) {
        Perfil perfil = perfilPadrao();

        Usuario usuario = new Usuario();
        usuario.setNome(nome == null || nome.isBlank() ? email : nome.trim());
        usuario.setEmail(email.trim().toLowerCase());
        usuario.setSenhaHash(passwordEncoder.encode("GOOGLE_LOGIN"));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    private Perfil perfilPadrao() {
        return perfilRepository.findByNome("USUARIO").orElseGet(() -> {
            Perfil novoPerfil = new Perfil();
            novoPerfil.setNome("USUARIO");
            novoPerfil.setDescricao("Usuario autorizado a enviar e consultar analises de documentos");
            return perfilRepository.save(novoPerfil);
        });
    }

    private String extrairEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = atributo(oauth2User, "email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Usuario autenticado nao identificado");
        }
        return email;
    }

    private String atributo(OAuth2User oauth2User, String nome) {
        Object valor = oauth2User.getAttribute(nome);
        return valor == null ? null : valor.toString();
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
