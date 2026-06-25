package com.trabalho.verificadocs.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "analises")
public class Analise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAnalise status = StatusAnalise.PENDENTE;

    @Column(name = "protocolo_externo", length = 80)
    private String protocoloExterno;

    @Column(name = "indicio_edicao", nullable = false)
    private boolean indicioEdicao;

    @Column(name = "modo_demonstracao", nullable = false)
    private boolean modoDemonstracao;

    @Column(name = "score_suspeita", precision = 5, scale = 2)
    private BigDecimal scoreSuspeita = BigDecimal.ZERO;

    @Column(name = "grau_confianca", precision = 5, scale = 2)
    private BigDecimal grauConfianca = BigDecimal.ZERO;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    @Column(name = "data_retorno")
    private LocalDateTime dataRetorno;

    @Column(name = "texto_extraido", columnDefinition = "text")
    private String textoExtraido;

    @Column(name = "resumo_resultado", columnDefinition = "text")
    private String resumoResultado;

    @Column(name = "mensagem_api", columnDefinition = "text")
    private String mensagemApi;

    @Column(name = "resposta_usuario", columnDefinition = "text")
    private String respostaUsuario;

    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "analise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnaliseOcorrencia> ocorrencias = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatusAnalise getStatus() {
        return status;
    }

    public void setStatus(StatusAnalise status) {
        this.status = status;
    }

    public String getProtocoloExterno() {
        return protocoloExterno;
    }

    public void setProtocoloExterno(String protocoloExterno) {
        this.protocoloExterno = protocoloExterno;
    }

    public boolean isIndicioEdicao() {
        return indicioEdicao;
    }

    public void setIndicioEdicao(boolean indicioEdicao) {
        this.indicioEdicao = indicioEdicao;
    }

    public boolean isModoDemonstracao() {
        return modoDemonstracao;
    }

    public void setModoDemonstracao(boolean modoDemonstracao) {
        this.modoDemonstracao = modoDemonstracao;
    }

    public BigDecimal getScoreSuspeita() {
        return scoreSuspeita;
    }

    public void setScoreSuspeita(BigDecimal scoreSuspeita) {
        this.scoreSuspeita = scoreSuspeita;
    }

    public BigDecimal getGrauConfianca() {
        return grauConfianca;
    }

    public void setGrauConfianca(BigDecimal grauConfianca) {
        this.grauConfianca = grauConfianca;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDateTime getDataRetorno() {
        return dataRetorno;
    }

    public void setDataRetorno(LocalDateTime dataRetorno) {
        this.dataRetorno = dataRetorno;
    }

    public String getTextoExtraido() {
        return textoExtraido;
    }

    public void setTextoExtraido(String textoExtraido) {
        this.textoExtraido = textoExtraido;
    }

    public String getResumoResultado() {
        return resumoResultado;
    }

    public void setResumoResultado(String resumoResultado) {
        this.resumoResultado = resumoResultado;
    }

    public String getMensagemApi() {
        return mensagemApi;
    }

    public void setMensagemApi(String mensagemApi) {
        this.mensagemApi = mensagemApi;
    }

    public String getRespostaUsuario() {
        return respostaUsuario;
    }

    public void setRespostaUsuario(String respostaUsuario) {
        this.respostaUsuario = respostaUsuario;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }

    public Documento getDocumento() {
        return documento;
    }

    public void setDocumento(Documento documento) {
        this.documento = documento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<AnaliseOcorrencia> getOcorrencias() {
        return ocorrencias;
    }

    public void setOcorrencias(List<AnaliseOcorrencia> ocorrencias) {
        this.ocorrencias = ocorrencias;
    }

    public void solicitar() {
        this.status = StatusAnalise.EM_PROCESSAMENTO;
        this.dataSolicitacao = LocalDateTime.now();
    }

    public void registrarResultado() {
        this.status = StatusAnalise.CONCLUIDA;
        this.dataRetorno = LocalDateTime.now();
    }

    public void registrarErro(String mensagem) {
        this.status = StatusAnalise.ERRO;
        this.resumoResultado = mensagem;
        this.dataRetorno = LocalDateTime.now();
    }

    public void adicionarOcorrencia(AnaliseOcorrencia ocorrencia) {
        ocorrencia.setAnalise(this);
        this.ocorrencias.add(ocorrencia);
    }

    public int getScorePercentual() {
        return scoreSuspeita == null ? 0 : scoreSuspeita.intValue();
    }

    public int getConfiancaPercentual() {
        return grauConfianca == null ? 0 : grauConfianca.intValue();
    }

    public String getNivelConfianca() {
        int confianca = getConfiancaPercentual();
        if (confianca >= 80) {
            return "Alta";
        }
        if (confianca >= 55) {
            return "Média";
        }
        return "Baixa";
    }

    public String getStatusConfianca() {
        int confianca = getConfiancaPercentual();
        if (confianca >= 80) {
            return "Confiável";
        }
        if (confianca >= 55) {
            return "Revisar";
        }
        return "Não confiável";
    }

    public String getClasseConfianca() {
        int confianca = getConfiancaPercentual();
        if (confianca >= 80) {
            return "alta";
        }
        if (confianca >= 55) {
            return "media";
        }
        return "baixa";
    }

    public String getNivelRisco() {
        int score = getScorePercentual();
        if (score >= 70) {
            return "Alto";
        }
        if (score >= 40) {
            return "Moderado";
        }
        return "Baixo";
    }
}
