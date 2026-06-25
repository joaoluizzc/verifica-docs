package com.trabalho.verificadocs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trabalho.verificadocs.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);
}
