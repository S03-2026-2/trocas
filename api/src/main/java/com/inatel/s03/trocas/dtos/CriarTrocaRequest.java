package com.inatel.s03.trocas.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarTrocaRequest(
        @NotBlank String idJogador,
        @NotBlank String idCartaOfertada,
        String idCartaDesejada, // opcional
        @NotNull @Min(1) Integer limitePropostas
) {
}
