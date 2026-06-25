package com.trabalho.verificadocs.service;

import java.math.BigDecimal;

public record OcorrenciaDetectada(
        String tipoOcorrencia,
        String campo,
        String valorEncontrado,
        BigDecimal confianca) {
}
