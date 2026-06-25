package com.trabalho.verificadocs.service;

public record OcrResultado(
        boolean sucesso,
        boolean modoDemonstracao,
        String protocoloExterno,
        String textoExtraido,
        String respostaBruta,
        String mensagemErro) {
}
