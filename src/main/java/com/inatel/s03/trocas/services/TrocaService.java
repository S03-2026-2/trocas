package com.inatel.s03.trocas.services;

import com.inatel.s03.trocas.troca.Troca;
import com.inatel.s03.trocas.troca.TrocaRepository;

public class TrocaService {
    private final TrocaRepository trocaRepository;

    public TrocaService(TrocaRepository trocaRepository) {
        this.trocaRepository = trocaRepository;
    }

    public Troca createTroca(String idJogador){
        return new Troca("1", "2", 3);
    }
}
