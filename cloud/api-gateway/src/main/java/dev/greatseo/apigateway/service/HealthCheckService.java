package dev.greatseo.apigateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class HealthCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);

    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public HealthCheckService(WebClient.Builder webClientBuilder
    ) {
        this.webClientBuilder = webClientBuilder;
    }

    @Bean(name = "Core System Microservices")
    ReactiveHealthContributor CoreServicesHealth() {

        ReactiveHealthIndicator productHealthIndicator = ()->this.getHealth("http://product:8080"),
                recommendationHealthIndicator = ()->this.getHealth("http://recommendation:8080"),
                reviewHealthIndicator = ()->this.getHealth("http://review:8080"),
                productCompositeHealthIndicator = ()->this.getHealth("http://product-composite:8080");

        Map<String, ReactiveHealthContributor> allIndicators = Map.of(
                "product", productHealthIndicator,
                "recommendation", recommendationHealthIndicator,
                "review", reviewHealthIndicator,
                "product-composite", productCompositeHealthIndicator
                );

        return CompositeReactiveHealthContributor.fromMap(allIndicators);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOGGER.debug("Will call the Health API on URL: {}", url);
        return getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }
}