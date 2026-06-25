package com.trabalho.verificadocs.service;

public record BancoResumo(
        String produto,
        String url,
        long documentos,
        long analises,
        long ocorrencias,
        long usuarios,
        long logsAcesso) {
}
