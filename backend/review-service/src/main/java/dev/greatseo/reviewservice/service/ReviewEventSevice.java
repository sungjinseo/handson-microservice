package dev.greatseo.reviewservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.product.ProductService;
import dev.greatseo.api.core.review.ReviewDto;
import dev.greatseo.api.core.review.ReviewService;
import dev.greatseo.api.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class ReviewEventSevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewEventSevice.class);

    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    @Autowired
    ReviewEventSevice(ReviewService reviewService){
        this.reviewService = reviewService;
        this.objectMapper = new ObjectMapper();
    }

    @Bean
    public Consumer<Event<Integer, String>> reviews() {
        return eventItem -> {
            try {
                LOGGER.info("review event-type: {}",eventItem.getEventType());
                ReviewDto item = objectMapper.readValue((String)eventItem.getValue(), ReviewDto.class);
                reviewService.createReview(item);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
