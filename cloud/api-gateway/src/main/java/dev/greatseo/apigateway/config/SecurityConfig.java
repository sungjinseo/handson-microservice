package dev.greatseo.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    SecurityWebFilterChain gatewaySecurityFilterChain(ServerHttpSecurity http) throws Exception {

        http
                .csrf(csrf->csrf.disable())
                .authorizeExchange(authReq->{
                    authReq.pathMatchers("/headerrouting/**").permitAll()
                            .pathMatchers("/actuator/**").permitAll()
                            .pathMatchers("/eureka/**").permitAll()
                            .pathMatchers("/oauth2/**").permitAll()
                            .pathMatchers("/login/**").permitAll()
                            .pathMatchers("/error/**").permitAll()
                            .pathMatchers("/openapi/**").permitAll()
                            .pathMatchers("/webjars/**").permitAll()
                            .anyExchange().authenticated();
                }).oauth2ResourceServer(resourceServer-> resourceServer.jwt(Customizer.withDefaults()));

        return http.build();

    }
}
