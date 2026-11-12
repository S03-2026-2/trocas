package com.inatel.s03.trocas.repositories;

import com.inatel.s03.trocas.troca.FiltroTrocas;
import com.inatel.s03.trocas.troca.StatusTroca;
import com.inatel.s03.trocas.troca.Troca;
import org.springframework.data.jpa.domain.Specification;

/**
 * Monta a consulta de trocas a partir dos filtros informados.
 * Cada filtro vira uma regra independente; filtros nulos são ignorados.
 */
public final class TrocaSpecs {

    private TrocaSpecs() {
    }

    public static Specification<Troca> comFiltro(FiltroTrocas filtro) {
        if (filtro == null) {
            return Specification.where(null);
        }
        return Specification.where(comStatus(filtro.status()))
                .and(doJogador(filtro.idJogador()));
    }

    private static Specification<Troca> comStatus(StatusTroca status) {
        return status == null ? null
                : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Troca> doJogador(String idJogador) {
        return idJogador == null || idJogador.isBlank() ? null
                : (root, query, cb) -> cb.equal(root.get("idJogador"), idJogador);
    }
}
