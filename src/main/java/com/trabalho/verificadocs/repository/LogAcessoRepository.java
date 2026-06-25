package com.trabalho.verificadocs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trabalho.verificadocs.model.LogAcesso;

public interface LogAcessoRepository extends JpaRepository<LogAcesso, Long> {
}
