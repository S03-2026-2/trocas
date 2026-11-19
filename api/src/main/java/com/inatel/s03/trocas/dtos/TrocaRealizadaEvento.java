package com.inatel.s03.trocas.dtos;

import java.time.Instant;

public record TrocaRealizadaEvento(
        String idEvento,
        String idTroca,
        String idJogadorOfertante,
        String idJogadorProponente,
        String idCartaOfertada,
        String idCartaRecebida,
        Instant realizadaEm
) {}
