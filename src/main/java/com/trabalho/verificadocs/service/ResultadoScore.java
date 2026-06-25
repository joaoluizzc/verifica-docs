package com.trabalho.verificadocs.service;

import java.math.BigDecimal;
import java.util.List;

public record ResultadoScore(
        BigDecimal scoreSuspeita,
        BigDecimal grauConfianca,
        boolean indicioEdicao,
        String resumo,
        List<OcorrenciaDetectada> ocorrencias) {
}
