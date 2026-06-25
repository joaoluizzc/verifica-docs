package com.trabalho.verificadocs.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.trabalho.verificadocs.model.Perfil;
import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.repository.PerfilRepository;
import com.trabalho.verificadocs.repository.UsuarioRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            PerfilRepository perfilRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Perfil perfil = perfilRepository.findByNome("USUARIO").orElseGet(() -> {
            Perfil novoPerfil = new Perfil();
            novoPerfil.setNome("USUARIO");
            novoPerfil.setDescricao("Usuario autorizado a enviar e consultar analises de documentos");
            return perfilRepository.save(novoPerfil);
        });

        usuarioRepository.findByEmailIgnoreCase("usuario@demo.com").orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNome("Usuario Demo");
            usuario.setEmail("usuario@demo.com");
            usuario.setSenhaHash(passwordEncoder.encode("123456"));
            usuario.setPerfil(perfil);
            usuario.setAtivo(true);
            return usuarioRepository.save(usuario);
        });
    }
}
