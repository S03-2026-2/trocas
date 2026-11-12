package com.inatel.s03.trocas.dtos;

import org.springframework.data.domain.Page;

/** Bloco "pagination" das respostas paginadas. As páginas começam em 1. */
public record Paginacao(
        long total,
        int page,
        int perPage,
        int lastPage
) {
    public static Paginacao de(Page<?> pagina) {
        return new Paginacao(
                pagina.getTotalElements(),
                pagina.getNumber() + 1,              // o Spring conta a partir de 0
                pagina.getSize(),
                Math.max(pagina.getTotalPages(), 1)  // sem resultados ainda existe a página 1 (vazia)
        );
    }
}
