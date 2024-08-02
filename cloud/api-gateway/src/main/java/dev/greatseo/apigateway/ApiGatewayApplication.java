package dev.greatseo.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ApiGatewayApplication {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    // bean으로 관리하는 것보단 spring config를 이용해서 properties로 관리하는게 나을거 같음
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
//                .route("catalog-router", r -> r.path("/catalog/**")
//                        .filters(f -> f.rewritePath("/catalog/(?<segment>.*)", "/${segment}"))
//                        .uri("lb://CATALOG"))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
