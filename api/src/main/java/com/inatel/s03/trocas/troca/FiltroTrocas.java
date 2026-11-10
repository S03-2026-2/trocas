package com.inatel.s03.trocas.troca;

/**
 * Filtros da listagem de trocas, preenchidos a partir da query string
 * (ex.: GET /trocas?status=ABERTA&idJogador=jogador1). Todos são opcionais.
 *
 * Para adicionar um filtro: crie o campo aqui e a regra correspondente em TrocaSpecs.
 */
public record FiltroTrocas(
        StatusTroca status,
        String idJogador
) {
}
