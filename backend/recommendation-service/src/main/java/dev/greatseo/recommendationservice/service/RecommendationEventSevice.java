package dev.greatseo.recommendationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.recommendation.RecommendationDto;
import dev.greatseo.api.core.recommendation.RecommendationService;
import dev.greatseo.api.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class RecommendationEventSevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationEventSevice.class);

    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    @Autowired
    RecommendationEventSevice(RecommendationService recommendationService){
        this.recommendationService = recommendationService;
        this.objectMapper = new ObjectMapper();
    }

    @Bean
    public Consumer<Event<Integer, String>> recommendation() {
        return eventItem -> {
            try {
                LOGGER.info("event-type: {}",eventItem.getEventType());
                RecommendationDto item = objectMapper.readValue((String)eventItem.getValue(), RecommendationDto.class);
                recommendationService.createRecommendation(item);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
