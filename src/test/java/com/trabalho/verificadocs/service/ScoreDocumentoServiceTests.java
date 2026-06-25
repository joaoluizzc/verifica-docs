package com.trabalho.verificadocs.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.trabalho.verificadocs.model.TipoDocumento;

class ScoreDocumentoServiceTests {

    private final ScoreDocumentoService service = new ScoreDocumentoService();

    @Test
    void devePermitirConfiancaTotalParaDocumentoCompleto() {
        String texto = """
                CARTEIRA NACIONAL DE HABILITACAO CNH
                Registro 12345678900 Categoria AB Validade 24/06/2030
                Condutor Joao da Silva CPF 529.982.247-25
                Documento emitido em 24/06/2026 pelo orgao de transito competente.
                Observacoes do documento, filiacao, local de nascimento e demais campos legiveis.
                Numero do espelho, assinatura do emissor, assinatura do condutor, local de emissao,
                informacoes administrativas e campos complementares preservados e legiveis.
                """;

        ResultadoScore resultado = service.calcular(texto, TipoDocumento.CC, false);

        assertThat(resultado.grauConfianca()).isEqualByComparingTo("100.00");
        assertThat(resultado.scoreSuspeita()).isEqualByComparingTo("0.00");
    }

    @Test
    void deveVariarScoreDemonstrativoConformeConteudo() {
        ResultadoScore primeiro = service.calcular(
                "Analise demonstrativa gerada. Arquivo recebido: documento-a.jfif",
                TipoDocumento.CC,
                true);
        ResultadoScore segundo = service.calcular(
                "Analise demonstrativa gerada. Arquivo recebido: contrato-2026.pdf",
                TipoDocumento.CONTRATO,
                true);

        assertThat(primeiro.scoreSuspeita()).isNotEqualByComparingTo(segundo.scoreSuspeita());
    }
}
