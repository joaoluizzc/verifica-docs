package com.trabalho.verificadocs.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.trabalho.verificadocs.repository.AnaliseOcorrenciaRepository;
import com.trabalho.verificadocs.repository.AnaliseRepository;
import com.trabalho.verificadocs.repository.DocumentoRepository;
import com.trabalho.verificadocs.repository.LogAcessoRepository;
import com.trabalho.verificadocs.repository.UsuarioRepository;

@Service
public class BancoDiagnosticoService {

    private final DataSource dataSource;
    private final DocumentoRepository documentoRepository;
    private final AnaliseRepository analiseRepository;
    private final AnaliseOcorrenciaRepository analiseOcorrenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAcessoRepository logAcessoRepository;

    public BancoDiagnosticoService(
            DataSource dataSource,
            DocumentoRepository documentoRepository,
            AnaliseRepository analiseRepository,
            AnaliseOcorrenciaRepository analiseOcorrenciaRepository,
            UsuarioRepository usuarioRepository,
            LogAcessoRepository logAcessoRepository) {
        this.dataSource = dataSource;
        this.documentoRepository = documentoRepository;
        this.analiseRepository = analiseRepository;
        this.analiseOcorrenciaRepository = analiseOcorrenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.logAcessoRepository = logAcessoRepository;
    }

    public BancoResumo gerarResumo() {
        return new BancoResumo(
                produtoBanco(),
                urlBanco(),
                documentoRepository.count(),
                analiseRepository.count(),
                analiseOcorrenciaRepository.count(),
                usuarioRepository.count(),
                logAcessoRepository.count());
    }

    private String produtoBanco() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        } catch (SQLException ex) {
            return "Indisponivel";
        }
    }

    private String urlBanco() {
        try (Connection connection = dataSource.getConnection()) {
            return limparUrl(connection.getMetaData().getURL());
        } catch (SQLException ex) {
            return "Sem conexao";
        }
    }

    private String limparUrl(String url) {
        if (url == null || url.isBlank()) {
            return "Nao informado";
        }

        int parametros = url.indexOf('?');
        return parametros >= 0 ? url.substring(0, parametros) : url;
    }
}
