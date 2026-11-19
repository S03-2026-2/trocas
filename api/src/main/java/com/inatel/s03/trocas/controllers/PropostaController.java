package com.inatel.s03.trocas.controllers;

import com.inatel.s03.trocas.dtos.ConsultarPropostasRequest;
import com.inatel.s03.trocas.dtos.CriarPropostaRequest;
import com.inatel.s03.trocas.dtos.PaginaResponse;
import com.inatel.s03.trocas.dtos.PropostaResponse;
import com.inatel.s03.trocas.proposta.Proposta;
import com.inatel.s03.trocas.services.PropostaService;
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
@RequestMapping("/propostas")
@RequiredArgsConstructor
public class PropostaController {

    private final PropostaService propostaService;

    @PostMapping
    public ResponseEntity<PropostaResponse> criar(@RequestBody @Valid CriarPropostaRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        Proposta proposta = propostaService.criarProposta(
                request.idTroca(),
                request.idJogador(),
                request.idCartaOfertada()
        );
        URI location = uriBuilder.path("/propostas/{id}").buildAndExpand(proposta.getId()).toUri();
        return ResponseEntity.created(location).body(PropostaResponse.de(proposta));
    }

    @GetMapping
    public PaginaResponse<PropostaResponse> listar(
            @RequestBody @Valid ConsultarPropostasRequest request,
            @PageableDefault(size = 20, sort = "criadaEm", direction = Sort.Direction.ASC) Pageable paginacao) {
        return PaginaResponse.de(propostaService.consultarPropostas(request.idTroca(), paginacao),
                PropostaResponse::de);
    }

    @GetMapping("/{id}")
    public PropostaResponse buscar(@PathVariable String id) {
        return PropostaResponse.de(propostaService.buscarProposta(id));
    }

    @PostMapping("/{id}/aceitar")
    public PropostaResponse aceitar(@PathVariable String id) {
        return PropostaResponse.de(propostaService.aceitarProposta(id));
    }

    @PostMapping("/{id}/cancelar")
    public PropostaResponse cancelar(@PathVariable String id) {
        return PropostaResponse.de(propostaService.cancelarProposta(id));
    }
}