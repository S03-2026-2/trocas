package com.inatel.s03.trocas.troca;

public class Troca {
    private String id;
    private String idJogador;
    //to do: status ENUM
    private int limitePropostas;
    //to do: cartaOfertada
    //to do: cartaDesejada
    //to do propostas (list propostas)

    public Troca(String id, String idJogador, int limitePropostas) {
        this.id = id;
        this.idJogador = idJogador;
        this.limitePropostas = limitePropostas;
    }
}
