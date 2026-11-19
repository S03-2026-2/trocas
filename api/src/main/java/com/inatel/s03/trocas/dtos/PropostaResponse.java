package com.inatel.s03.trocas.dtos;

import com.inatel.s03.trocas.proposta.Proposta;
import com.inatel.s03.trocas.proposta.StatusProposta;

import java.time.Instant;

public record PropostaResponse(
        String id,
        String idTroca,
        String idJogador,
        String idCartaOfertada,
        StatusProposta status,
        Instant criadaEm
) {
    public static PropostaResponse de(Proposta proposta) {
        return new PropostaResponse(
                proposta.getId(),
                proposta.getIdTroca(),
                proposta.getIdJogador(),
                proposta.getIdCartaOfertada(),
                proposta.getStatus(),
                proposta.getCriadaEm()
        );
    }
}