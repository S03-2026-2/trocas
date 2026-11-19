package com.inatel.s03.trocas.repositories;

import com.inatel.s03.trocas.proposta.FiltroPropostas;
import com.inatel.s03.trocas.proposta.Proposta;
import com.inatel.s03.trocas.proposta.StatusProposta;
import org.springframework.data.jpa.domain.Specification;

public final class PropostaSpecs {

    private PropostaSpecs() {
    }

    public static Specification<Proposta> comFiltro(FiltroPropostas filtro) {
        if (filtro == null) {
            return Specification.where(null);
        }
        return Specification.where(daTroca(filtro.idTroca()))
                .and(comStatus(filtro.status()))
                .and(doJogador(filtro.idJogador()));
    }

    private static Specification<Proposta> daTroca(String idTroca) {
        return idTroca == null || idTroca.isBlank() ? null
                : (root, query, cb) -> cb.equal(root.get("idTroca"), idTroca);
    }

    private static Specification<Proposta> comStatus(StatusProposta status) {
        return status == null ? null
                : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Proposta> doJogador(String idJogador) {
        return idJogador == null || idJogador.isBlank() ? null
                : (root, query, cb) -> cb.equal(root.get("idJogador"), idJogador);
    }
}