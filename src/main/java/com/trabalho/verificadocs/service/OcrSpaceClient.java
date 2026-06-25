package com.trabalho.verificadocs.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.trabalho.verificadocs.config.OcrSpaceProperties;

@Service
public class OcrSpaceClient {

    private static final int TOTAL_TENTATIVAS = 3;

    private final OcrSpaceProperties properties;
    private final RestClient restClient;

    public OcrSpaceClient(OcrSpaceProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public OcrResultado analisar(MultipartFile arquivo) {
        if (!properties.isEnabled() || properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return resultadoDemonstrativo(arquivo, "API externa desativada ou sem chave configurada.");
        }

        Exception ultimaFalha = null;
        for (int tentativa = 1; tentativa <= TOTAL_TENTATIVAS; tentativa++) {
            try {
                return executarAnalise(arquivo);
            } catch (Exception ex) {
                ultimaFalha = ex;
                aguardarAntesDeTentarNovamente(tentativa);
            }
        }

        String mensagem = ultimaFalha == null ? "Falha desconhecida na API externa." : ultimaFalha.getMessage();
        return resultadoDemonstrativo(arquivo, "Nao foi possivel consultar OCR.space apos nova tentativa: " + mensagem);
    }

    private OcrResultado executarAnalise(MultipartFile arquivo) throws IOException {
        try {
            Map<String, Object> resposta = restClient.post()
                    .uri(properties.getApiUrl())
                    .header("apikey", properties.getApiKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(montarFormulario(arquivo))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (resposta == null) {
                throw new IllegalStateException("A API externa nao retornou corpo de resposta.");
            }

            String respostaBruta = serializar(resposta);
            boolean erro = Boolean.TRUE.equals(resposta.get("IsErroredOnProcessing"));
            String texto = extrairTexto(resposta);
            String protocolo = "OCR-" + UUID.randomUUID();

            if (erro) {
                return resultadoDemonstrativo(arquivo, "OCR.space retornou erro: " + extrairMensagemErro(resposta));
            }

            return new OcrResultado(true, false, protocolo, texto, respostaBruta, null);
        } catch (IOException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        }
    }

    private void aguardarAntesDeTentarNovamente(int tentativa) {
        if (tentativa >= TOTAL_TENTATIVAS) {
            return;
        }

        try {
            Thread.sleep(900L * tentativa);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private MultiValueMap<String, Object> montarFormulario(MultipartFile arquivo) throws IOException {
        MultiValueMap<String, Object> formulario = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(arquivo.getBytes()) {
            @Override
            public String getFilename() {
                return nomeArquivoAceitoPelaApi(arquivo);
            }
        };

        formulario.add("file", resource);
        formulario.add("language", "por");
        formulario.add("isOverlayRequired", "false");
        formulario.add("OCREngine", "2");
        return formulario;
    }

    private String nomeArquivoAceitoPelaApi(MultipartFile arquivo) {
        String nomeOriginal = arquivo.getOriginalFilename();
        String nomeSeguro = nomeOriginal == null || nomeOriginal.isBlank()
                ? "documento"
                : nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");

        String nomeMinusculo = nomeSeguro.toLowerCase();
        if (nomeMinusculo.endsWith(".jfif")) {
            return substituirExtensao(nomeSeguro, ".jpg");
        }

        if (temExtensaoAceita(nomeMinusculo)) {
            return nomeSeguro;
        }

        String extensaoPorMime = extensaoPorMime(arquivo.getContentType());
        if (extensaoPorMime != null) {
            return substituirOuAdicionarExtensao(nomeSeguro, extensaoPorMime);
        }

        return substituirOuAdicionarExtensao(nomeSeguro, ".jpg");
    }

    private boolean temExtensaoAceita(String nomeArquivo) {
        return nomeArquivo.endsWith(".pdf")
                || nomeArquivo.endsWith(".jpg")
                || nomeArquivo.endsWith(".jpeg")
                || nomeArquivo.endsWith(".png")
                || nomeArquivo.endsWith(".bmp")
                || nomeArquivo.endsWith(".gif")
                || nomeArquivo.endsWith(".tif")
                || nomeArquivo.endsWith(".tiff")
                || nomeArquivo.endsWith(".webp");
    }

    private String extensaoPorMime(String contentType) {
        if (contentType == null) {
            return null;
        }

        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg", "image/jpg", "image/pjpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/bmp", "image/x-ms-bmp" -> ".bmp";
            case "image/gif" -> ".gif";
            case "image/tiff" -> ".tiff";
            case "image/webp" -> ".webp";
            default -> null;
        };
    }

    private String substituirOuAdicionarExtensao(String nomeArquivo, String extensao) {
        int ultimoPonto = nomeArquivo.lastIndexOf('.');
        if (ultimoPonto > 0 && ultimoPonto < nomeArquivo.length() - 1) {
            return substituirExtensao(nomeArquivo, extensao);
        }

        return nomeArquivo + extensao;
    }

    private String substituirExtensao(String nomeArquivo, String extensao) {
        int ultimoPonto = nomeArquivo.lastIndexOf('.');
        if (ultimoPonto <= 0) {
            return nomeArquivo + extensao;
        }

        return nomeArquivo.substring(0, ultimoPonto) + extensao;
    }

    private String extrairTexto(Map<String, Object> resposta) {
        Object parsedResults = resposta.get("ParsedResults");
        if (!(parsedResults instanceof List<?> resultados)) {
            return "";
        }

        List<String> textos = new ArrayList<>();
        for (Object item : resultados) {
            if (item instanceof Map<?, ?> resultado) {
                Object parsedText = resultado.get("ParsedText");
                if (parsedText != null) {
                    textos.add(parsedText.toString());
                }
            }
        }

        return String.join("\n", textos).trim();
    }

    private String extrairMensagemErro(Map<String, Object> resposta) {
        Object details = resposta.get("ErrorDetails");
        if (details instanceof List<?> erros && !erros.isEmpty()) {
            return erros.get(0).toString();
        }

        Object message = resposta.get("ErrorMessage");
        return message == null ? "Erro retornado pela API de OCR." : message.toString();
    }

    private String serializar(Map<String, Object> resposta) {
        return resposta.toString();
    }

    private OcrResultado resultadoDemonstrativo(MultipartFile arquivo, String motivo) {
        String texto = """
                Analise demonstrativa gerada pelo sistema.
                Arquivo recebido: %s
                O conteudo nao foi extraido por OCR real nesta execucao.
                Revise se o documento esta legivel, completo e sem sinais de edicao.
                """.formatted(arquivo.getOriginalFilename());

        return new OcrResultado(
                true,
                true,
                "DEMO-" + UUID.randomUUID(),
                texto,
                "{}",
                motivo);
    }
}
