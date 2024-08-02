package dev.greatseo.authservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSecurityConfig.class);

    // formatter:off
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrfConfig->csrfConfig.disable())
                .formLogin(withDefaults())
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
//                .headers((headerConfig)->
//                        headerConfig.frameOptions((frameOptionsConfig ->
//                                frameOptionsConfig.sameOrigin())
//                        )
//                )
//                .sessionManagement(sessionConfig->sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        ;
        return http.build();
    }
    // formatter:on

    // @formatter:off
    @Bean
    UserDetailsService users() {
        UserDetails user = User.builder()
                .username("u")
                .password(passwordEncoder().encode("p"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
    // @formatter:on

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
//CHECKSTYLE:ON