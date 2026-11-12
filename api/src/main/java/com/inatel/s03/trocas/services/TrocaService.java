package com.inatel.s03.trocas.services;

import com.inatel.s03.trocas.exceptions.ParametroInvalidoException;
import com.inatel.s03.trocas.exceptions.RecursoNaoEncontradoException;
import com.inatel.s03.trocas.exceptions.RegraNegocioException;
import com.inatel.s03.trocas.integracoes.Inventario;
import com.inatel.s03.trocas.repositories.TrocaRepository;
import com.inatel.s03.trocas.repositories.TrocaSpecs;
import com.inatel.s03.trocas.troca.FiltroTrocas;
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
public class TrocaService {

    /** Campos que o cliente pode usar em ?sort=. Campos internos (ex.: versao) ficam de fora. */
    private static final List<String> CAMPOS_ORDENAVEIS = List.of("criadaEm", "idJogador", "limitePropostas", "status");

    private final TrocaRepository trocaRepository;
    private final Inventario inventario;

    /** Caso de uso "Criar troca" (inclui "Validar carta requerida"). */
    @Transactional
    public Troca criarTroca(String idJogador, String idCartaOfertada, String idCartaDesejada, int limitePropostas) {
        Troca troca = new Troca(idJogador, idCartaOfertada, idCartaDesejada, limitePropostas);

        if (!inventario.possuiCarta(idJogador, idCartaOfertada)) {
            throw new RegraNegocioException(
                    "O jogador " + idJogador + " não possui a carta " + idCartaOfertada);
        }

        return trocaRepository.save(troca);
    }

    /** Casos de uso "Consultar trocas" / "Listar trocas" (Painel ADM). */
    @Transactional(readOnly = true)
    public Page<Troca> consultarTrocas(FiltroTrocas filtro, Pageable paginacao) {
        validarOrdenacao(paginacao.getSort());

        // Desempate pelo id: sem uma ordem totalmente definida, itens com o mesmo valor
        // de ordenação podem se repetir ou sumir entre uma página e outra.
        Pageable comDesempate = PageRequest.of(paginacao.getPageNumber(), paginacao.getPageSize(),
                paginacao.getSort().and(Sort.by("id")));

        return trocaRepository.findAll(TrocaSpecs.comFiltro(filtro), comDesempate);
    }

    @Transactional(readOnly = true)
    public Troca buscarTroca(String idTroca) {
        return trocaRepository.findById(idTroca)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Troca " + idTroca + " não encontrada"));
    }

    /** Caso de uso "Cancelar troca". */
    @Transactional
    public Troca cancelarTroca(String idTroca) {
        Troca troca = buscarTroca(idTroca);
        troca.cancelar();
        // TODO (propostas): cancelar as propostas PENDENTES desta troca na mesma transação.
        return troca; // alterações salvas automaticamente no commit (entidade gerenciada)
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
