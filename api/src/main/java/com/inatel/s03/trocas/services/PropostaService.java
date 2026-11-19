package com.inatel.s03.trocas.services;

import com.inatel.s03.trocas.exceptions.ParametroInvalidoException;
import com.inatel.s03.trocas.exceptions.RecursoNaoEncontradoException;
import com.inatel.s03.trocas.exceptions.RegraNegocioException;
import com.inatel.s03.trocas.integracoes.Inventario;
import com.inatel.s03.trocas.proposta.FiltroPropostas;
import com.inatel.s03.trocas.proposta.Proposta;
import com.inatel.s03.trocas.proposta.StatusProposta;
import com.inatel.s03.trocas.repositories.PropostaRepository;
import com.inatel.s03.trocas.repositories.PropostaSpecs;
import com.inatel.s03.trocas.troca.Troca;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropostaService {
    private static final List<String> CAMPOS_ORDENAVEIS = List.of("criadaEm", "idJogador", "status");

    private final PropostaRepository propostaRepository;
    private final TrocaService trocaService;
    private final Inventario inventario;

    @Transactional
    public Proposta criarProposta(String idTroca, String idJogador, String idCartaOfertada) {
        Troca troca = trocaService.buscarTroca(idTroca);
        Proposta proposta = new Proposta(idTroca, idJogador, idCartaOfertada);

        if (!troca.estaAberta()) {
            throw new RegraNegocioException(
                    "A troca " + idTroca + " não aceita propostas: status atual é " + troca.getStatus());
        }
        if (troca.getIdJogador().equals(idJogador)) {
            throw new RegraNegocioException("O dono da troca não pode enviar proposta para a própria troca");
        }

        if (troca.getIdCartaDesejada() != null && !troca.getIdCartaDesejada().equals(idCartaOfertada)) {
            throw new RegraNegocioException(
                    "A troca pede a carta " + troca.getIdCartaDesejada() + ", não a carta " + idCartaOfertada);
        }

        long pendentes = propostaRepository.countByIdTrocaAndStatus(idTroca, StatusProposta.PENDENTE);
        if (pendentes >= troca.getLimitePropostas()) {
            throw new RegraNegocioException(
                    "A troca " + idTroca + " já atingiu o limite de " + troca.getLimitePropostas() + " propostas");
        }
        if (!inventario.possuiCarta(idJogador, idCartaOfertada)) {
            throw new RegraNegocioException(
                    "O jogador " + idJogador + " não possui a carta " + idCartaOfertada);
        }

        return propostaRepository.save(proposta);
    }

    /** Caso de uso "Consultar propostas de uma troca", com filtros opcionais de status e jogador. */
    @Transactional(readOnly = true)
    public Page<Proposta> consultarPropostas(FiltroPropostas filtro, Pageable paginacao) {
        trocaService.buscarTroca(filtro.idTroca()); // 404 se a troca não existir, em vez de uma lista vazia
        validarOrdenacao(paginacao.getSort());

        // Desempate pelo id: sem uma ordem totalmente definida, itens com o mesmo valor
        // de ordenação podem se repetir ou sumir entre uma página e outra.
        Pageable comDesempate = PageRequest.of(paginacao.getPageNumber(), paginacao.getPageSize(),
                paginacao.getSort().and(Sort.by("id")));

        return propostaRepository.findAll(PropostaSpecs.comFiltro(filtro), comDesempate);
    }

    /** Todas as propostas de uma troca, sem filtros (usado por GET /trocas/{id}/propostas). */
    @Transactional(readOnly = true)
    public Page<Proposta> consultarPropostas(String idTroca, Pageable paginacao) {
        return consultarPropostas(new FiltroPropostas(idTroca, null, null), paginacao);
    }

    @Transactional(readOnly = true)
    public Proposta buscarProposta(String idProposta) {
        return propostaRepository.findById(idProposta)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Proposta " + idProposta + " não encontrada"));
    }

    @Transactional
    public Proposta aceitarProposta(String idProposta) {
        Proposta aceita = buscarProposta(idProposta);
        Troca troca = trocaService.buscarTroca(aceita.getIdTroca());

        aceita.aceitar();
        troca.concluir();

        propostaRepository.findByIdTrocaAndStatus(troca.getId(), StatusProposta.PENDENTE)
                .forEach(Proposta::recusar);

        return aceita;
    }

    /** Caso de uso "Cancelar proposta". */
    @Transactional
    public Proposta cancelarProposta(String idProposta) {
        Proposta proposta = buscarProposta(idProposta);
        proposta.cancelar();
        return proposta;
    }

    private static void validarOrdenacao(Sort ordenacao) {
        for (Sort.Order ordem : ordenacao) {
            if (!CAMPOS_ORDENAVEIS.contains(ordem.getProperty())) {
                throw new ParametroInvalidoException("Não é possível ordenar por '" + ordem.getProperty()
                        + "'. Campos permitidos: " + String.join(", ", CAMPOS_ORDENAVEIS));
            }
        }
    }
}