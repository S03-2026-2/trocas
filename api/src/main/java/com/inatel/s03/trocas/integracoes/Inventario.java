package com.inatel.s03.trocas.integracoes;

/**
 * Consulta ao inventário de cartas dos jogadores, que pertence a outro sistema
 * (Distribuição de Cartas). Os services dependem desta interface, não da implementação.
 */
public interface Inventario {

    boolean possuiCarta(String idJogador, String idCarta);
}
