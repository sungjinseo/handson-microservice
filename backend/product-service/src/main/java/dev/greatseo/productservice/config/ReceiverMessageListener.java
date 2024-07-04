package dev.greatseo.productservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.product.ProductService;
import dev.greatseo.api.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class ReceiverMessageListener {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Autowired
    ReceiverMessageListener(ProductService productService){
        this.productService = productService;
        this.objectMapper = new ObjectMapper();
    }

    // blocking coding시 cosumer ack 처리로 인한 키중복 발생
    @Bean
    public Function<Event, ProductDto> products() {
        return eventItem -> {
            try {
                ProductDto item = objectMapper.readValue(eventItem.getValue().toString(), ProductDto.class);
                return productService.createProduct(item);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
