package com.magedata.fxprocessor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${fx.api.timeout.connect-millis:3000}")
    private int connectTimeoutMillis;

    @Value("${fx.api.timeout.read-millis:4000}")
    private int readTimeoutMillis;

    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMillis));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
