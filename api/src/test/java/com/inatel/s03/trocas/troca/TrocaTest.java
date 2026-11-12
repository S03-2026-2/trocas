package com.inatel.s03.trocas.troca;

import com.inatel.s03.trocas.exceptions.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrocaTest {

    @Test
    void novaTrocaComecaAberta() {
        Troca troca = new Troca("jogador1", "c1", "c2", 3);

        assertThat(troca.getId()).isNotBlank();
        assertThat(troca.getStatus()).isEqualTo(StatusTroca.ABERTA);
        assertThat(troca.getCriadaEm()).isNotNull();
    }

    @Test
    void cartaDesejadaEhOpcional() {
        Troca troca = new Troca("jogador1", "c1", null, 3);

        assertThat(troca.getIdCartaDesejada()).isNull();
    }

    @Test
    void cartaOfertadaEhObrigatoria() {
        assertThatThrownBy(() -> new Troca("jogador1", " ", null, 3))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoPermiteDesejarAMesmaCartaOfertada() {
        assertThatThrownBy(() -> new Troca("jogador1", "c1", "c1", 3))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void limiteDePropostasPrecisaSerPositivo() {
        assertThatThrownBy(() -> new Troca("jogador1", "c1", null, 0))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cancelarTrocaAberta() {
        Troca troca = new Troca("jogador1", "c1", null, 3);

        troca.cancelar();

        assertThat(troca.getStatus()).isEqualTo(StatusTroca.CANCELADA);
    }

    @Test
    void naoCancelaTrocaJaCancelada() {
        Troca troca = new Troca("jogador1", "c1", null, 3);
        troca.cancelar();

        assertThatThrownBy(troca::cancelar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoCancelaTrocaConcluida() {
        Troca troca = new Troca("jogador1", "c1", null, 3);
        troca.concluir();

        assertThatThrownBy(troca::cancelar).isInstanceOf(RegraNegocioException.class);
    }
}
