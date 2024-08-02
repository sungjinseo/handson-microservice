package dev.greatseo.productservice.service;

import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.product.ProductService;
import dev.greatseo.productservice.repository.ProductEntity;
import dev.greatseo.productservice.repository.ProductRepository;
import dev.greatseo.util.exceptions.InvalidInputException;
import dev.greatseo.util.exceptions.NotFoundException;
import dev.greatseo.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

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
    public Mono<ProductDto> createProduct(ProductDto body) {

        if (body.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.productId());
        }

        ProductEntity entity = mapper.apiToEntity(body);
        Mono<ProductDto> newEntity = repository.save(entity)
                .log(LOGGER.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.productId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity;

    }

    /**
     * @param productId
     * @return
     */
    @Override
    public Mono<ProductDto> getProduct(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(LOGGER.getName(), FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> {
                    LOGGER.info("Find the item : {}", e.toString());
                    return new ProductDto(e.productId(), e.name(), e.weight(), serviceUtil.getServiceAddress());
                });

    }

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productId
     */
    @Override
    public Mono<Void> deleteProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOGGER.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId)
                .log(LOGGER.getName(), FINE)
                .map(e -> repository.delete(e))
                .flatMap(e -> e);
    }
}
