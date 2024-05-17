package dev.greatseo.productservice.service;

import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.productservice.repository.ProductEntity;
import dev.greatseo.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dev.greatseo.api.core.product.ProductService;
import dev.greatseo.util.exceptions.InvalidInputException;
import dev.greatseo.util.exceptions.NotFoundException;
import dev.greatseo.util.http.ServiceUtil;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;

    private final ProductRepository repository;

    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/product \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body
     * @return
     */
    @Override
    public ResponseEntity<ProductDto> createProduct(ProductDto body) {
        try{
            ProductEntity entity = repository.save(mapper.apiToEntity(body));
            LOGGER.debug("createProduct: entity created for productId: {}", body.productId());
            return ResponseEntity.created(URI.create("/product/" + entity.getProductId())).build();

        } catch (DuplicateKeyException e){
            throw new InvalidInputException("Duplicate key, Product Id: " + body.productId());
        }
    }

    /**
     * @param productId
     * @return
     */
    @Override
    public Mono<ProductDto> getProduct(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        ProductDto response = mapper.entityToApi(entity);
        LOGGER.debug("getProduct: found productId: {}", response.productId());

        return Mono.just(response);

    }

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productId
     */
    @Override
    public ResponseEntity deleteProduct(int productId) {
        LOGGER.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).ifPresent(repository::delete);

        return ResponseEntity.noContent().build();
    }
}
