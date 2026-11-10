package com.inatel.s03.trocas.exceptions;

/** Parâmetro de consulta com valor não permitido (ex.: ordenar por um campo não suportado). Vira HTTP 400. */
public class ParametroInvalidoException extends RuntimeException {

    public ParametroInvalidoException(String mensagem) {
        super(mensagem);
    }
}
