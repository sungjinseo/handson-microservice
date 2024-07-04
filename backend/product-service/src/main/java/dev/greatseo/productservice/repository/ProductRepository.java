package dev.greatseo.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductRepository extends ReactiveMongoRepository<ProductEntity, String> {
    Mono<ProductEntity> findByProductId(int productId);
}