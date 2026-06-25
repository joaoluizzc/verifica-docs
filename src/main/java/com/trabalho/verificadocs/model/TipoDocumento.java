package com.trabalho.verificadocs.model;

public enum TipoDocumento {
    CC("CNH"),
    CPF("CPF"),
    RG("RG"),
    CONTRATO("Contrato"),
    OUTRO("Outro");

    private final String descricao;

    TipoDocumento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
