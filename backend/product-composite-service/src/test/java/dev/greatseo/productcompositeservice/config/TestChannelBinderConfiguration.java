package dev.greatseo.productcompositeservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.config.BinderFactoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.config.EnableIntegration;

@Configuration
@ConditionalOnMissingBean({Binder.class})
@Import({BinderFactoryAutoConfiguration.class})
@EnableIntegration
public class TestChannelBinderConfiguration {

    @Bean
    public InputDestination sourceDestination() {
        return new InputDestination();
    }

    @Bean
    public OutputDestination targetDestination() {
        return new OutputDestination();
    }
}
