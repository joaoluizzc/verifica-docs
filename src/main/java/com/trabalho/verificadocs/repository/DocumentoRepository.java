package com.trabalho.verificadocs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trabalho.verificadocs.model.Documento;
import com.trabalho.verificadocs.model.Usuario;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByUsuarioOrderByDataUploadDesc(Usuario usuario);
}
