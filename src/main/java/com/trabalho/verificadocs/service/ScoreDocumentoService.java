package com.trabalho.verificadocs.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.trabalho.verificadocs.model.TipoDocumento;

@Service
public class ScoreDocumentoService {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b");
    private static final Pattern DATA_PATTERN = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{4}\\b");
    private static final Pattern PALAVRA_PATTERN = Pattern.compile("\\b[a-zA-Z0-9]{2,}\\b");
    private static final List<String> TERMOS_SUSPEITOS = List.of(
            "rasura", "alterado", "adulterado", "falsificado", "falsificacao", "copia", "montagem",
            "invalido", "editado");

    public ResultadoScore calcular(String textoExtraido, TipoDocumento tipoDocumento, boolean modoDemonstracao) {
        String texto = textoExtraido == null ? "" : textoExtraido.trim();
        String normalizado = normalizar(texto);
        int score = modoDemonstracao ? 18 : 0;
        List<OcorrenciaDetectada> ocorrencias = new ArrayList<>();

        int penalidadeTamanho = calcularPenalidadeTamanho(texto, tipoDocumento, modoDemonstracao);
        if (penalidadeTamanho > 0) {
            score += penalidadeTamanho;
            ocorrencias.add(ocorrencia(
                    "LEGIBILIDADE",
                    "texto_extraido",
                    "Texto extraido abaixo do volume esperado",
                    58 + penalidadeTamanho));
        }

        int penalidadeQualidade = calcularPenalidadeQualidade(texto, normalizado);
        if (penalidadeQualidade > 0) {
            score += penalidadeQualidade;
            ocorrencias.add(ocorrencia(
                    "QUALIDADE_OCR",
                    "texto_extraido",
                    "OCR com baixa estrutura de leitura",
                    52 + penalidadeQualidade));
        }

        boolean cpfEncontrado = CPF_PATTERN.matcher(texto).find();
        if (!cpfEncontrado && exigeCpf(tipoDocumento)) {
            int penalidadeCpf = 10 + Math.min(8, Math.max(0, 180 - texto.length()) / 35);
            score += penalidadeCpf;
            ocorrencias.add(ocorrencia("CAMPO_AUSENTE", "cpf", "CPF nao localizado no OCR", 60 + penalidadeCpf));
        } else if (cpfEncontrado && !cpfValido(texto)) {
            score += 12;
            ocorrencias.add(ocorrencia("CAMPO_INVALIDO", "cpf", "CPF localizado com digitos invalidos", 72));
        }

        if (exigeData(tipoDocumento) && !DATA_PATTERN.matcher(texto).find()) {
            int penalidadeData = texto.length() < 120 ? 10 : 6;
            score += penalidadeData;
            ocorrencias.add(ocorrencia("CAMPO_AUSENTE", "data", "Data nao localizada no OCR", 58 + penalidadeData));
        }

        int penalidadeTipo = calcularPenalidadeTipoDocumento(normalizado, tipoDocumento);
        if (penalidadeTipo > 0) {
            score += penalidadeTipo;
            ocorrencias.add(ocorrencia(
                    "TIPO_DOCUMENTO",
                    "conteudo",
                    "Poucas palavras esperadas para o tipo selecionado",
                    56 + penalidadeTipo));
        }

        for (String termo : TERMOS_SUSPEITOS) {
            if (normalizado.contains(termo)) {
                int penalidadeTermo = 12 + Math.min(8, termo.length());
                score += penalidadeTermo;
                ocorrencias.add(ocorrencia("INDICIO_EDICAO", "conteudo", "Termo suspeito: " + termo, 82));
            }
        }

        if (modoDemonstracao) {
            score += ajusteFinoPorConteudo(texto, tipoDocumento, true);
            ocorrencias.add(ocorrencia("API", "ocr", "Resultado gerado em modo demonstrativo", 55));
        } else if (score > 0) {
            score += ajusteFinoPorConteudo(texto, tipoDocumento, false);
        }

        score = Math.max(0, Math.min(score, 100));
        BigDecimal scoreDecimal = BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
        BigDecimal confianca = BigDecimal.valueOf(100 - score).setScale(2, RoundingMode.HALF_UP);
        boolean indicioEdicao = score >= 65 || ocorrencias.stream()
                .anyMatch(ocorrencia -> "INDICIO_EDICAO".equals(ocorrencia.tipoOcorrencia()));

        String resumo = montarResumo(score, indicioEdicao, modoDemonstracao);
        return new ResultadoScore(scoreDecimal, confianca, indicioEdicao, resumo, ocorrencias);
    }

    private String montarResumo(int score, boolean indicioEdicao, boolean modoDemonstracao) {
        String risco;
        if (score >= 70) {
            risco = "alto";
        } else if (score >= 40) {
            risco = "moderado";
        } else {
            risco = "baixo";
        }

        String origem = modoDemonstracao
                ? "A analise usou o modo demonstrativo porque a API externa nao retornou OCR real."
                : "A analise foi calculada a partir do texto extraido pela API externa.";

        return "Risco " + risco + " de inconsistencia documental. "
                + (indicioEdicao ? "Foram encontrados indicios que merecem revisao manual. "
                        : "Nao foram encontrados indicios fortes de edicao. ")
                + origem;
    }

    private int calcularPenalidadeTamanho(String texto, TipoDocumento tipoDocumento, boolean modoDemonstracao) {
        if (texto.isBlank()) {
            return modoDemonstracao ? 26 : 42;
        }

        int esperado = tamanhoEsperado(tipoDocumento);
        if (texto.length() >= esperado) {
            return 0;
        }

        double deficit = (esperado - texto.length()) / (double) esperado;
        int penalidade = (int) Math.round(deficit * 28);
        return modoDemonstracao ? Math.max(6, penalidade) : Math.max(2, penalidade);
    }

    private int calcularPenalidadeQualidade(String texto, String normalizado) {
        if (texto.isBlank()) {
            return 18;
        }

        int palavras = contarPalavras(normalizado);
        long digitos = texto.chars().filter(Character::isDigit).count();
        int penalidade = 0;

        if (palavras < 10) {
            penalidade += 12 - palavras;
        }

        if (digitos == 0) {
            penalidade += 4;
        }

        if (texto.contains("�") || normalizado.contains("???")) {
            penalidade += 8;
        }

        return Math.min(18, Math.max(0, penalidade));
    }

    private int calcularPenalidadeTipoDocumento(String normalizado, TipoDocumento tipoDocumento) {
        List<String> termos = termosEsperados(tipoDocumento);
        if (termos.isEmpty() || normalizado.isBlank()) {
            return 0;
        }

        long encontrados = termos.stream()
                .filter(normalizado::contains)
                .count();

        if (encontrados >= 3) {
            return 0;
        }

        if (encontrados == 2) {
            return 3;
        }

        if (encontrados == 1) {
            return 7;
        }

        return 12;
    }

    private List<String> termosEsperados(TipoDocumento tipoDocumento) {
        return switch (tipoDocumento) {
            case CC -> List.of("cnh", "habilitacao", "condutor", "carteira", "categoria", "validade", "registro");
            case CPF -> List.of("cpf", "cadastro", "pessoa", "fisica", "receita", "federal");
            case RG -> List.of("rg", "registro", "geral", "identidade", "emissor", "nascimento", "expedicao");
            case CONTRATO -> List.of("contrato", "contratante", "contratada", "clausula", "assinatura", "valor");
            case OUTRO -> List.of();
        };
    }

    private int tamanhoEsperado(TipoDocumento tipoDocumento) {
        return switch (tipoDocumento) {
            case CONTRATO -> 520;
            case CC -> 230;
            case RG -> 180;
            case CPF -> 90;
            case OUTRO -> 120;
        };
    }

    private boolean exigeCpf(TipoDocumento tipoDocumento) {
        return tipoDocumento == TipoDocumento.CC || tipoDocumento == TipoDocumento.CPF;
    }

    private boolean exigeData(TipoDocumento tipoDocumento) {
        return tipoDocumento == TipoDocumento.CC
                || tipoDocumento == TipoDocumento.RG
                || tipoDocumento == TipoDocumento.CONTRATO;
    }

    private int ajusteFinoPorConteudo(String texto, TipoDocumento tipoDocumento, boolean modoDemonstracao) {
        if (texto.isBlank()) {
            return 0;
        }

        int palavras = contarPalavras(normalizar(texto));
        int digitos = (int) texto.chars().filter(Character::isDigit).count();
        int base = Math.abs((texto.length() * 3) + (palavras * 5) + (digitos * 7) + tipoDocumento.name().hashCode());
        int ajuste = base % 7;

        if (modoDemonstracao) {
            return ajuste;
        }

        return Math.max(0, ajuste - 3);
    }

    private int contarPalavras(String normalizado) {
        Matcher matcher = PALAVRA_PATTERN.matcher(normalizado);
        int total = 0;
        while (matcher.find()) {
            total++;
        }
        return total;
    }

    private boolean cpfValido(String texto) {
        Matcher matcher = CPF_PATTERN.matcher(texto);
        while (matcher.find()) {
            String cpf = matcher.group().replaceAll("\\D", "");
            if (cpfDigitosValidos(cpf)) {
                return true;
            }
        }

        return false;
    }

    private boolean cpfDigitosValidos(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }

        return digitoCpfValido(cpf, 9) && digitoCpfValido(cpf, 10);
    }

    private boolean digitoCpfValido(String cpf, int posicao) {
        int soma = 0;
        for (int i = 0; i < posicao; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (posicao + 1 - i);
        }

        int resto = soma % 11;
        int digito = resto < 2 ? 0 : 11 - resto;
        return digito == Character.getNumericValue(cpf.charAt(posicao));
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private OcorrenciaDetectada ocorrencia(String tipo, String campo, String valor, int confianca) {
        return new OcorrenciaDetectada(
                tipo,
                campo,
                valor,
                BigDecimal.valueOf(Math.min(confianca, 99)).setScale(2, RoundingMode.HALF_UP));
    }
}
