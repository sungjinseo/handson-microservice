package dev.greatseo.api.core.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequestMapping("/product")
public interface ProductService {
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/product \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body
     * @return
     */
    @PostMapping(
            value    = "/",
            consumes = "application/json",
            produces = "application/json")
    ProductDto createProduct(@RequestBody ProductDto body);

    /**
     * Sample usage: curl $HOST:$PORT/product/1
     *
     * @param productId
     * @return the product, if found, else null
     */
    @GetMapping(
            value    = "/{productId}",
            produces = "application/json")
    Mono<ProductDto> getProduct(@PathVariable int productId);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productId
     */
    @DeleteMapping(value = "/{productId}")
    ResponseEntity deleteProduct(@PathVariable int productId);
}
