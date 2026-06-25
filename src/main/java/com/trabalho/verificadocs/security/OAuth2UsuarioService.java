package com.trabalho.verificadocs.security;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.service.UsuarioService;

@Service
public class OAuth2UsuarioService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UsuarioService usuarioService;

    public OAuth2UsuarioService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User usuarioGoogle = delegate.loadUser(userRequest);
        String email = atributo(usuarioGoogle, "email");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_nao_retornado"));
        }

        Usuario usuario = usuarioService.buscarOuCriarUsuarioExterno(nomeUsuario(usuarioGoogle, email), email);

        if (!usuario.isAtivo()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("usuario_inativo"));
        }

        Set<GrantedAuthority> authorities = new LinkedHashSet<>(usuarioGoogle.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().getNome()));

        return new DefaultOAuth2User(authorities, usuarioGoogle.getAttributes(), "email");
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
