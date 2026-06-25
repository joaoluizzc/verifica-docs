package com.trabalho.verificadocs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trabalho.verificadocs.model.Analise;
import com.trabalho.verificadocs.model.Usuario;

public interface AnaliseRepository extends JpaRepository<Analise, Long> {

    @EntityGraph(attributePaths = {"documento"})
    List<Analise> findByUsuarioOrderByDataSolicitacaoDesc(Usuario usuario);

    @EntityGraph(attributePaths = {"documento", "ocorrencias"})
    Optional<Analise> findByIdAndUsuario(Long id, Usuario usuario);
}
