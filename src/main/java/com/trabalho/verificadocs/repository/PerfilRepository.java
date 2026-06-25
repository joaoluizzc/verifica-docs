package com.trabalho.verificadocs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trabalho.verificadocs.model.Perfil;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

    Optional<Perfil> findByNome(String nome);
}
