package com.trabalho.verificadocs.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        String perfil = usuario.getPerfil().getNome();
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .disabled(!usuario.isAtivo())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + perfil)))
                .build();
    }
}
