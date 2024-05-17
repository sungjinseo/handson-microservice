package dev.greatseo.productservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("dev.greatseo")
public class ProductServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceApplication.class);

    public static void main(String[] args) {
        //SpringApplication.run(ProductServiceApplication.class, args);
        ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);
        String mongDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
        LOGGER.info("Connected to MongoDB: " + mongDbHost + ":" + mongDbPort);
    }

}