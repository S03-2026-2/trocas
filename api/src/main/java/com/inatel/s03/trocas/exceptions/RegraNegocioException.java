package com.inatel.s03.trocas.exceptions;

/** Uma regra de negócio foi violada (ex.: cancelar troca já concluída). Vira HTTP 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
