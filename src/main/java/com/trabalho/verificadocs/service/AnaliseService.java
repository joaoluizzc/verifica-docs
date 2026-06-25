package com.trabalho.verificadocs.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.trabalho.verificadocs.model.Analise;
import com.trabalho.verificadocs.model.AnaliseOcorrencia;
import com.trabalho.verificadocs.model.Documento;
import com.trabalho.verificadocs.model.StatusDocumento;
import com.trabalho.verificadocs.model.TipoDocumento;
import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.repository.AnaliseRepository;
import com.trabalho.verificadocs.repository.DocumentoRepository;

@Service
public class AnaliseService {

    private final DocumentoRepository documentoRepository;
    private final AnaliseRepository analiseRepository;
    private final StorageService storageService;
    private final OcrSpaceClient ocrSpaceClient;
    private final ScoreDocumentoService scoreDocumentoService;

    public AnaliseService(
            DocumentoRepository documentoRepository,
            AnaliseRepository analiseRepository,
            StorageService storageService,
            OcrSpaceClient ocrSpaceClient,
            ScoreDocumentoService scoreDocumentoService) {
        this.documentoRepository = documentoRepository;
        this.analiseRepository = analiseRepository;
        this.storageService = storageService;
        this.ocrSpaceClient = ocrSpaceClient;
        this.scoreDocumentoService = scoreDocumentoService;
    }

    @Transactional
    public Analise analisarDocumento(Usuario usuario, MultipartFile arquivo, TipoDocumento tipoDocumento)
            throws IOException {
        validarArquivo(arquivo);

        Documento documento = new Documento();
        documento.setUsuario(usuario);
        documento.setTipo(tipoDocumento);
        documento.setNomeOriginal(arquivo.getOriginalFilename());
        documento.setMimeType(arquivo.getContentType() == null ? "application/octet-stream" : arquivo.getContentType());
        documento.setTamanhoBytes(arquivo.getSize());
        documento.setCaminhoArquivo(storageService.salvar(arquivo));
        documento.setStatus(documento.validarMimeType() ? StatusDocumento.ATIVO : StatusDocumento.ERRO);
        documentoRepository.save(documento);

        Analise analise = new Analise();
        analise.setUsuario(usuario);
        analise.setDocumento(documento);
        analise.solicitar();
        analiseRepository.save(analise);

        OcrResultado ocr = ocrSpaceClient.analisar(arquivo);
        ResultadoScore resultado = scoreDocumentoService.calcular(
                ocr.textoExtraido(),
                tipoDocumento,
                ocr.modoDemonstracao());

        analise.setProtocoloExterno(ocr.protocoloExterno());
        analise.setTextoExtraido(ocr.textoExtraido());
        analise.setModoDemonstracao(ocr.modoDemonstracao());
        analise.setMensagemApi(ocr.mensagemErro());
        analise.setScoreSuspeita(resultado.scoreSuspeita());
        analise.setGrauConfianca(resultado.grauConfianca());
        analise.setIndicioEdicao(resultado.indicioEdicao());
        analise.setResumoResultado(resultado.resumo());

        for (OcorrenciaDetectada ocorrenciaDetectada : resultado.ocorrencias()) {
            analise.adicionarOcorrencia(criarOcorrencia(ocorrenciaDetectada));
        }

        if (!ocr.sucesso()) {
            analise.adicionarOcorrencia(criarOcorrencia(new OcorrenciaDetectada(
                    "API",
                    "ocr",
                    ocr.mensagemErro(),
                    resultado.grauConfianca())));
        }

        analise.registrarResultado();
        return analiseRepository.save(analise);
    }

    @Transactional(readOnly = true)
    public List<Analise> listarAnalises(Usuario usuario) {
        return analiseRepository.findByUsuarioOrderByDataSolicitacaoDesc(usuario);
    }

    @Transactional(readOnly = true)
    public Analise buscarAnaliseDoUsuario(Long id, Usuario usuario) {
        return analiseRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Analise nao encontrada."));
    }

    @Transactional
    public Analise registrarRespostaUsuario(Long id, Usuario usuario, String respostaUsuario) {
        Analise analise = buscarAnaliseDoUsuario(id, usuario);
        analise.setRespostaUsuario(respostaUsuario);
        analise.setAtualizadaEm(LocalDateTime.now());
        return analiseRepository.save(analise);
    }

    private AnaliseOcorrencia criarOcorrencia(OcorrenciaDetectada detectada) {
        AnaliseOcorrencia ocorrencia = new AnaliseOcorrencia();
        ocorrencia.setTipoOcorrencia(detectada.tipoOcorrencia());
        ocorrencia.setCampo(detectada.campo());
        ocorrencia.setValorEncontrado(detectada.valorEncontrado());
        ocorrencia.setConfianca(detectada.confianca());
        ocorrencia.registrar();
        return ocorrencia;
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo para analise.");
        }

        String contentType = arquivo.getContentType();
        boolean permitido = contentType != null
                && (contentType.equals("application/pdf") || contentType.startsWith("image/"));

        if (!permitido) {
            throw new IllegalArgumentException("Envie um PDF ou imagem.");
        }
    }
}
