package com.inatel.s03.trocas.integracoes.broker;

import com.inatel.s03.trocas.dtos.TrocaRealizadaEvento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BrokerClient {

    private final RestClient brokerRestClient;

    public void publicar(TrocaRealizadaEvento evento) {
    }
}