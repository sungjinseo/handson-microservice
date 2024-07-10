package dev.greatseo.api.composite;

import dev.greatseo.api.composite.product.ProductAggregate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductCompositeService {

    /**
     * Sample usage: curl $HOST:$PORT/product-composite/1
     *
     * @param productId
     * @return the composite product info, if found, else null
     */
    @GetMapping(
            value    = "/product-composite/{productId}",
            produces = "application/json")
    Mono<ProductAggregate> getCompositeProduct(@PathVariable int productId);

    @PostMapping(
            value    = "/product-composite",
            consumes = "application/json")
    void createCompositeProduct(@RequestBody ProductAggregate body);

    @DeleteMapping(value = "/product-composite/{productId}")
    void deleteCompositeProduct(@PathVariable int productId);
}
