package com.trabalho.verificadocs.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "analises_ocorrencias")
public class AnaliseOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_ocorrencia", nullable = false, length = 80)
    private String tipoOcorrencia;

    @Column(nullable = false, length = 120)
    private String campo;

    @Column(name = "valor_encontrado", length = 500)
    private String valorEncontrado;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal confianca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analise_id", nullable = false)
    private Analise analise;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoOcorrencia() {
        return tipoOcorrencia;
    }

    public void setTipoOcorrencia(String tipoOcorrencia) {
        this.tipoOcorrencia = tipoOcorrencia;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorEncontrado() {
        return valorEncontrado;
    }

    public void setValorEncontrado(String valorEncontrado) {
        this.valorEncontrado = valorEncontrado;
    }

    public BigDecimal getConfianca() {
        return confianca;
    }

    public void setConfianca(BigDecimal confianca) {
        this.confianca = confianca;
    }

    public Analise getAnalise() {
        return analise;
    }

    public void setAnalise(Analise analise) {
        this.analise = analise;
    }

    public void registrar() {
        if (this.confianca == null) {
            this.confianca = BigDecimal.ZERO;
        }
    }
}
