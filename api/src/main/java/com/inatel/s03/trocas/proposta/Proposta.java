package com.inatel.s03.trocas.proposta;

import com.inatel.s03.trocas.exceptions.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "proposta")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // exigido pelo JPA
@EqualsAndHashCode(of = "id")
public class Proposta {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String idTroca;

    @Column(nullable = false)
    private String idJogador;

    @Column(nullable = false)
    private String idCartaOfertada;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatusProposta status;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    public Proposta(String idTroca, String idJogador, String idCartaOfertada) {
        if (idTroca == null || idTroca.isBlank()) {
            throw new RegraNegocioException("O id da troca é obrigatório");
        }
        if (idJogador == null || idJogador.isBlank()) {
            throw new RegraNegocioException("O id do jogador é obrigatório");
        }
        if (idCartaOfertada == null || idCartaOfertada.isBlank()) {
            throw new RegraNegocioException("A carta ofertada é obrigatória");
        }
        this.id = UUID.randomUUID().toString();
        this.idTroca = idTroca;
        this.idJogador = idJogador;
        this.idCartaOfertada = idCartaOfertada;
        this.status = StatusProposta.PENDENTE;
        this.criadaEm = Instant.now();
    }

    public boolean estaPendente() {
        return status == StatusProposta.PENDENTE;
    }

    public void aceitar() {
        exigirPendente("aceitar");
        this.status = StatusProposta.ACEITA;
    }

    public void recusar() {
        exigirPendente("recusar");
        this.status = StatusProposta.RECUSADA;
    }

    public void cancelar() {
        exigirPendente("cancelar");
        this.status = StatusProposta.CANCELADA;
    }

    private void exigirPendente(String acao) {
        if (!estaPendente()) {
            throw new RegraNegocioException(
                    "Não é possível " + acao + " a proposta " + id + ": status atual é " + status);
        }
    }
}