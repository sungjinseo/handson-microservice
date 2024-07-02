package dev.greatseo.reviewservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@SpringBootApplication
@ComponentScan("dev.greatseo")
public class ReviewServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceApplication.class);

    private final Integer connectionPoolSize;

    @Autowired
    public ReviewServiceApplication(
            @Value("${spring.datasource.maximum-pool-size:10}")
            Integer connectionPoolSize){
        this.connectionPoolSize = connectionPoolSize;
    }

    @Bean
    public Scheduler jdbcSchduler(){
        LOGGER.info("Creates a jdbcScheduler with pool size {}", connectionPoolSize);
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }

    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }

}
