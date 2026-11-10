package com.inatel.s03.trocas.exceptions;

/** O recurso pedido (troca, proposta...) não existe. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
