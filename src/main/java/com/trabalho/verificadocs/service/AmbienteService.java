package com.trabalho.verificadocs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.trabalho.verificadocs.config.OcrSpaceProperties;

@Service
public class AmbienteService {

    private final String datasourceUrl;
    private final OcrSpaceProperties ocrSpaceProperties;

    public AmbienteService(
            @Value("${spring.datasource.url:}") String datasourceUrl,
            OcrSpaceProperties ocrSpaceProperties) {
        this.datasourceUrl = datasourceUrl == null ? "" : datasourceUrl;
        this.ocrSpaceProperties = ocrSpaceProperties;
    }

    public String getBancoAtual() {
        if (datasourceUrl.contains("postgresql")) {
            return "PostgreSQL";
        }

        if (datasourceUrl.contains("h2:")) {
            return "H2 local";
        }

        return "Banco configurado";
    }

    public String getBancoDescricao() {
        if (datasourceUrl.contains("postgresql")) {
            return "Gravando em tabelas PostgreSQL";
        }

        if (datasourceUrl.contains("h2:")) {
            return "Ambiente local em memoria";
        }

        return "Persistencia configurada";
    }

    public String getOcrAtual() {
        if (!ocrSpaceProperties.isEnabled()) {
            return "OCR desativado";
        }

        if (ocrSpaceProperties.getApiKey() == null || ocrSpaceProperties.getApiKey().isBlank()) {
            return "OCR sem chave";
        }

        if ("helloworld".equalsIgnoreCase(ocrSpaceProperties.getApiKey())) {
            return "OCR.space teste";
        }

        return "OCR.space ativo";
    }
}
