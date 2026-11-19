package com.inatel.s03.trocas.integracoes.broker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BrokerConfig {

    @Bean
    public RestClient brokerRestClient(
            @Value("${broker.url}") String brokerUrl) {

        return RestClient.builder()
                .baseUrl(brokerUrl)
                .build();
    }
}