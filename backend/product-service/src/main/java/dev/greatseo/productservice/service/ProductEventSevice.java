package dev.greatseo.productservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.product.ProductService;
import dev.greatseo.api.event.Event;
import dev.greatseo.util.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ProductEventSevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventSevice.class);

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Autowired
    ProductEventSevice(ProductService productService){
        this.productService = productService;
        this.objectMapper = new ObjectMapper();
    }

    @Bean
    public Consumer<Event<Integer, String>> product() {
        return eventItem -> {
            try {
                LOGGER.info("event-type: {}",eventItem.getEventType());
                ProductDto item = objectMapper.readValue((String)eventItem.getValue(), ProductDto.class);
                productService.createProduct(item);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public Consumer<Event<Integer, ProductDto>> messageProcessor() {
        return event -> {
            LOGGER.info("Process message created at {}...", event.getEventCreatedTime());

            switch (event.getEventType()) {

                case CREATE:
                    ProductDto product = event.getValue();
                    LOGGER.info("Create product with ID: {}", product.productId());
                    productService.createProduct(product).block();
                    break;

                case DELETE:
                    int productId = event.getKey();
                    LOGGER.info("Delete product with ProductID: {}", productId);
                    productService.deleteProduct(productId).block();
                    break;

                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    LOGGER.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
            }

            LOGGER.info("Message processing done!");

        };
    }
}
