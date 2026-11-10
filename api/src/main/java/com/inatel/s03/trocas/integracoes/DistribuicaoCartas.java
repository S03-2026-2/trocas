package com.inatel.s03.trocas.integracoes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementação de {@link Inventario} que conversa com o sistema de Distribuição de Cartas.
 *
 * TODO: trocar a simulação por uma chamada HTTP real (RestClient) quando a API
 *  do serviço de Distribuição de Cartas estiver disponível.
 */
@Slf4j
@Component
public class DistribuicaoCartas implements Inventario {

    @Override
    public boolean possuiCarta(String idJogador, String idCarta) {
        log.warn("[SIMULADO] Inventário não integrado: assumindo que o jogador {} possui a carta {}",
                idJogador, idCarta);
        return true;
    }
}
