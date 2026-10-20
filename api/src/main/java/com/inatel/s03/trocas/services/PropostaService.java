package com.inatel.s03.trocas.services;

import com.inatel.s03.trocas.proposta.Proposta;
import com.inatel.s03.trocas.proposta.PropostaRepository;

public class PropostaService {
    private final PropostaRepository propostaRepository;

    public  PropostaService(PropostaRepository propostaRepository, PropostaRepository propostaRepository1) {
        this.propostaRepository = propostaRepository1;
    }

    public Proposta createProposta(String idJogador){
        return new Proposta("1", "2");
    }
}
