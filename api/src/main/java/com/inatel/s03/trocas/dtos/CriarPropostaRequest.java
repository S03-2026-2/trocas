package com.inatel.s03.trocas.dtos;

import jakarta.validation.constraints.NotBlank;

public record CriarPropostaRequest(
        @NotBlank String idTroca,
        @NotBlank String idJogador,
        @NotBlank String idCartaOfertada
) {
}
