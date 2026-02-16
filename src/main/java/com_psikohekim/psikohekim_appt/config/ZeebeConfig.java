package com_psikohekim.psikohekim_appt.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeConfig {

    @Value("${zeebe.client.broker.gatewayAddress:localhost:26500}")
    private String gatewayAddress;

    @Bean
    public ZeebeClient zeebeClient() {
        ZeebeClientBuilder builder = ZeebeClient.newClientBuilder()
                .gatewayAddress(gatewayAddress)
                .usePlaintext();
        return builder.build();
    }
}
