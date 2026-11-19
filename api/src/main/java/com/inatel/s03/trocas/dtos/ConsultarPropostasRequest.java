package com.inatel.s03.trocas.dtos;

import jakarta.validation.constraints.NotBlank;

public record ConsultarPropostasRequest(@NotBlank String idTroca) {
}