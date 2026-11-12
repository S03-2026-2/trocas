package com.inatel.s03.trocas.dtos;

import com.inatel.s03.trocas.troca.StatusTroca;
import com.inatel.s03.trocas.troca.Troca;

import java.time.Instant;

public record TrocaResponse(
        String id,
        String idJogador,
        StatusTroca status,
        int limitePropostas,
        String idCartaOfertada,
        String idCartaDesejada,
        Instant criadaEm
) {
    public static TrocaResponse de(Troca troca) {
        return new TrocaResponse(
                troca.getId(),
                troca.getIdJogador(),
                troca.getStatus(),
                troca.getLimitePropostas(),
                troca.getIdCartaOfertada(),
                troca.getIdCartaDesejada(),
                troca.getCriadaEm()
        );
    }
}
