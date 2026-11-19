package com.inatel.s03.trocas.dtos;

import com.inatel.s03.trocas.proposta.FiltroPropostas;
import com.inatel.s03.trocas.proposta.StatusProposta;
import jakarta.validation.constraints.NotBlank;

public record ConsultarPropostasRequest(
        @NotBlank String idTroca,
        StatusProposta status,
        String idJogador
) {
    public FiltroPropostas paraFiltro() {
        return new FiltroPropostas(idTroca, status, idJogador);
    }
}