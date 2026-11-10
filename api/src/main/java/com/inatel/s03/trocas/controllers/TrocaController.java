package com.inatel.s03.trocas.controllers;

import com.inatel.s03.trocas.dtos.CriarTrocaRequest;
import com.inatel.s03.trocas.dtos.TrocaResponse;
import com.inatel.s03.trocas.services.TrocaService;
import com.inatel.s03.trocas.troca.StatusTroca;
import com.inatel.s03.trocas.troca.Troca;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/trocas")
@RequiredArgsConstructor
public class TrocaController {

    private final TrocaService trocaService;

    /** 201 Created, com o header Location apontando para a troca criada. */
    @PostMapping
    public ResponseEntity<TrocaResponse> criar(@RequestBody @Valid CriarTrocaRequest request,
                                               UriComponentsBuilder uriBuilder) {
        Troca troca = trocaService.criarTroca(
                request.idJogador(),
                request.idCartaOfertada(),
                request.idCartaDesejada(),
                request.limitePropostas()
        );
        URI location = uriBuilder.path("/trocas/{id}").buildAndExpand(troca.getId()).toUri();
        return ResponseEntity.created(location).body(TrocaResponse.de(troca));
    }

    /** GET /trocas ou GET /trocas?status=ABERTA */
    @GetMapping
    public List<TrocaResponse> listar(@RequestParam(required = false) StatusTroca status) {
        return trocaService.consultarTrocas(status).stream()
                .map(TrocaResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public TrocaResponse buscar(@PathVariable String id) {
        return TrocaResponse.de(trocaService.buscarTroca(id));
    }

    @PostMapping("/{id}/cancelar")
    public TrocaResponse cancelar(@PathVariable String id) {
        return TrocaResponse.de(trocaService.cancelarTroca(id));
    }
}
