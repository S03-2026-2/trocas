package com.inatel.s03.trocas.controllers;

import com.inatel.s03.trocas.dtos.CriarTrocaRequest;
import com.inatel.s03.trocas.dtos.PaginaResponse;
import com.inatel.s03.trocas.dtos.PropostaResponse;
import com.inatel.s03.trocas.dtos.TrocaResponse;
import com.inatel.s03.trocas.services.PropostaService;
import com.inatel.s03.trocas.services.TrocaService;
import com.inatel.s03.trocas.troca.FiltroTrocas;
import com.inatel.s03.trocas.troca.Troca;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/trocas")
@RequiredArgsConstructor
public class TrocaController {

    private final TrocaService trocaService;
    private final PropostaService propostaService;

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

    /**
     * GET /trocas?status=ABERTA&idJogador=j1&page=1&perPage=20&sort=criadaEm,desc
     * Todos os parâmetros são opcionais. Padrão: página 1, 20 por página, mais recentes primeiro.
     */
    @GetMapping
    public PaginaResponse<TrocaResponse> listar(
            FiltroTrocas filtro,
            @PageableDefault(size = 20, sort = "criadaEm", direction = Sort.Direction.DESC) Pageable paginacao) {
        return PaginaResponse.de(trocaService.consultarTrocas(filtro, paginacao), TrocaResponse::de);
    }

    @GetMapping("/{id}")
    public TrocaResponse buscar(@PathVariable String id) {
        return TrocaResponse.de(trocaService.buscarTroca(id));
    }

    @PostMapping("/{id}/cancelar")
    public TrocaResponse cancelar(@PathVariable String id) {
        return TrocaResponse.de(trocaService.cancelarTroca(id));
    }

    /**
     * GET /trocas/{id}/propostas?page=1&perPage=20&sort=criadaEm,asc
     * Todas as propostas da troca, sem filtros. Para filtrar por status ou jogador, use POST /propostas/consultar.
     */
    @GetMapping("/{id}/propostas")
    public PaginaResponse<PropostaResponse> listarPropostas(
            @PathVariable String id,
            @PageableDefault(size = 20, sort = "criadaEm", direction = Sort.Direction.ASC) Pageable paginacao) {
        return PaginaResponse.de(propostaService.consultarPropostas(id, paginacao), PropostaResponse::de);
    }

}
