package com.inatel.s03.trocas.dtos;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Formato das respostas de listagem: os itens em "data" e os dados da paginação em "pagination".
 * Usamos um DTO próprio em vez de serializar o Page do Spring, para o JSON não mudar se o Spring mudar.
 */
public record PaginaResponse<T>(
        List<T> data,
        Paginacao pagination
) {
    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> conversor) {
        return new PaginaResponse<>(
                pagina.getContent().stream().map(conversor).toList(),
                Paginacao.de(pagina)
        );
    }
}
