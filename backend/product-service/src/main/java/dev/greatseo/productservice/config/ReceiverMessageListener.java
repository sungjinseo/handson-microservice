package dev.greatseo.productservice.config;

import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.product.ProductService;
import dev.greatseo.api.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SerializationUtils;

import java.util.function.Function;

@Configuration
public class ReceiverMessageListener {

    private final ProductService productService;

    @Autowired
    ReceiverMessageListener(ProductService productService){
        this.productService = productService;
    }

    @Bean
    public Function<Event, Void> products() {
        return eventItem -> {

            //(ProductDto) SerializationUtils.deserialize(eventItem.getValue());

            //ProductDto temp = (ProductDto) ;
            //System.out.println(temp.name());
            return null;
            //ResponseEntity<ProductDto> resultDto = productService.createProduct((ProductDto) eventItem.getValue());
            //return resultDto;
        };
    }
}
