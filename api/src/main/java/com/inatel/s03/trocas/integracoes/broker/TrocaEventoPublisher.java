package com.inatel.s03.trocas.integracoes.broker;

import com.inatel.s03.trocas.dtos.TrocaRealizadaEvento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrocaEventoPublisher {

    private final BrokerClient brokerClient;

    public void publicar(TrocaRealizadaEvento evento) {
        brokerClient.publicar(evento);
    }
}