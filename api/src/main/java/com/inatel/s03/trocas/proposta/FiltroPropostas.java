package com.inatel.s03.trocas.proposta;

public record FiltroPropostas(
        String idTroca,
        StatusProposta status,
        String idJogador
) {
}