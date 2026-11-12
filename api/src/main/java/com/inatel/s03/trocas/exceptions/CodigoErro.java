package com.inatel.s03.trocas.exceptions;

/**
 * Valores do campo "code" nas respostas de erro.
 * Permitem ao cliente identificar o motivo do erro sem depender do texto de "detail".
 */
public enum CodigoErro {
    VALIDATION_ERROR,
    INVALID_PARAMETER,
    MALFORMED_REQUEST,
    NOT_FOUND,
    BUSINESS_RULE_VIOLATION,
    CONFLICT,
    REQUEST_ERROR,
    INTERNAL_ERROR
}
