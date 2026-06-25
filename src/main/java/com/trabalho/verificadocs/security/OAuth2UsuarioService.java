package com.trabalho.verificadocs.security;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trabalho.verificadocs.model.Perfil;
import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.repository.PerfilRepository;
import com.trabalho.verificadocs.repository.UsuarioRepository;

@Service
public class OAuth2UsuarioService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2UsuarioService(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User usuarioGoogle = delegate.loadUser(userRequest);
        String email = atributo(usuarioGoogle, "email");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_nao_retornado"));
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> criarUsuarioGoogle(usuarioGoogle, email));

        if (!usuario.isAtivo()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("usuario_inativo"));
        }

        Set<GrantedAuthority> authorities = new LinkedHashSet<>(usuarioGoogle.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().getNome()));

        return new DefaultOAuth2User(authorities, usuarioGoogle.getAttributes(), "email");
    }

    private Usuario criarUsuarioGoogle(OAuth2User usuarioGoogle, String email) {
        Perfil perfil = perfilRepository.findByNome("USUARIO").orElseGet(() -> {
            Perfil novoPerfil = new Perfil();
            novoPerfil.setNome("USUARIO");
            novoPerfil.setDescricao("Usuario autorizado a enviar e consultar analises de documentos");
            return perfilRepository.save(novoPerfil);
        });

        Usuario usuario = new Usuario();
        usuario.setNome(nomeUsuario(usuarioGoogle, email));
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    private String nomeUsuario(OAuth2User usuarioGoogle, String email) {
        String nome = atributo(usuarioGoogle, "name");
        return nome == null || nome.isBlank() ? email : nome;
    }

    private String atributo(OAuth2User usuarioGoogle, String nome) {
        Object valor = usuarioGoogle.getAttribute(nome);
        return valor == null ? null : valor.toString();
    }
}
