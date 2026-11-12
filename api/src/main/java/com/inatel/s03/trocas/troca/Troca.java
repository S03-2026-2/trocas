package com.inatel.s03.trocas.troca;

import com.inatel.s03.trocas.exceptions.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Anúncio de troca: um jogador oferece uma carta e (opcionalmente) diz qual carta deseja.
 * As cartas pertencem ao serviço de Distribuição de Cartas; aqui guardamos só os ids.
 * As propostas ficam em outro agregado (Proposta referencia a troca por idTroca).
 */
@Entity
@Table(name = "trocas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // exigido pelo JPA
@EqualsAndHashCode(of = "id")
public class Troca {

    @Id
    private String id;

    @Column(nullable = false)
    private String idJogador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTroca status;

    @Column(nullable = false)
    private int limitePropostas;

    @Column(nullable = false)
    private String idCartaOfertada;

    /** Opcional: se nulo, o jogador aceita propostas com qualquer carta. */
    private String idCartaDesejada;

    /** Usado para ordenar as listagens (paginação precisa de uma ordem estável). */
    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    /** Controle de concorrência otimista (ex.: cancelar e aceitar proposta ao mesmo tempo). */
    @Version
    private Long versao;

    public Troca(String idJogador, String idCartaOfertada, String idCartaDesejada, int limitePropostas) {
        if (idJogador == null || idJogador.isBlank()) {
            throw new RegraNegocioException("O id do jogador é obrigatório");
        }
        if (idCartaOfertada == null || idCartaOfertada.isBlank()) {
            throw new RegraNegocioException("A carta ofertada é obrigatória");
        }
        if (idCartaDesejada != null && idCartaDesejada.isBlank()) {
            throw new RegraNegocioException("A carta desejada, se informada, não pode ser vazia");
        }
        if (idCartaOfertada.equals(idCartaDesejada)) {
            throw new RegraNegocioException("A carta desejada não pode ser a mesma carta ofertada");
        }
        if (limitePropostas < 1) {
            throw new RegraNegocioException("O limite de propostas deve ser de pelo menos 1");
        }
        this.id = UUID.randomUUID().toString();
        this.idJogador = idJogador;
        this.idCartaOfertada = idCartaOfertada;
        this.idCartaDesejada = idCartaDesejada;
        this.limitePropostas = limitePropostas;
        this.status = StatusTroca.ABERTA;
        this.criadaEm = Instant.now();
    }

    public boolean estaAberta() {
        return status == StatusTroca.ABERTA;
    }

    public void cancelar() {
        exigirAberta("cancelar");
        this.status = StatusTroca.CANCELADA;
    }

    /** Chamado quando uma proposta é aceita. */
    public void concluir() {
        exigirAberta("concluir");
        this.status = StatusTroca.CONCLUIDA;
    }

    private void exigirAberta(String acao) {
        if (!estaAberta()) {
            throw new RegraNegocioException(
                    "Não é possível " + acao + " a troca " + id + ": status atual é " + status);
        }
    }
}
