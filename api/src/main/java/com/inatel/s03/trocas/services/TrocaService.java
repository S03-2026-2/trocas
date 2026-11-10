package com.inatel.s03.trocas.services;

import com.inatel.s03.trocas.exceptions.RecursoNaoEncontradoException;
import com.inatel.s03.trocas.exceptions.RegraNegocioException;
import com.inatel.s03.trocas.integracoes.Inventario;
import com.inatel.s03.trocas.repositories.TrocaRepository;
import com.inatel.s03.trocas.troca.StatusTroca;
import com.inatel.s03.trocas.troca.Troca;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrocaService {

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

    /** Casos de uso "Consultar trocas" / "Listar trocas" (Painel ADM). Status é um filtro opcional. */
    @Transactional(readOnly = true)
    public List<Troca> consultarTrocas(StatusTroca status) {
        return status == null
                ? trocaRepository.findAll()
                : trocaRepository.findByStatus(status);
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
}
