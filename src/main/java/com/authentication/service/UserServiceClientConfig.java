package com.authentication.service;

import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class UserServiceClientConfig {
    private final ReactorLoadBalancerExchangeFilterFunction lbFunction;

    public UserServiceClientConfig(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        this.lbFunction = lbFunction;
    }

    @Bean
    public HttpGraphQlClient userGraphQlClient(
            WebClient.Builder webClientBuilder
    ) {
        WebClient webClient = webClientBuilder
                .baseUrl("http://users-service/graphql")
                .filter(lbFunction)
                .build();

        return HttpGraphQlClient.builder(webClient).build();
    }
}

