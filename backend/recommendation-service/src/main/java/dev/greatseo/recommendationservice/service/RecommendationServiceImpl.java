package dev.greatseo.recommendationservice.service;

import com.mongodb.DuplicateKeyException;
import dev.greatseo.api.core.recommendation.RecommendationDto;
import dev.greatseo.api.core.recommendation.RecommendationService;
import dev.greatseo.recommendationservice.repository.RecommendationEntity;
import dev.greatseo.recommendationservice.repository.RecommendationRepository;
import dev.greatseo.util.exceptions.InvalidInputException;
import dev.greatseo.util.exceptions.NotFoundException;
import dev.greatseo.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static reactor.core.publisher.Flux.error;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final RecommendationMapper mapper;
    private final RecommendationRepository repository;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationMapper mapper, RecommendationRepository repository) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<RecommendationDto> getRecommendations(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(error(new NotFoundException("No recommendation found for productId: "+ productId)))
                .log()
                .map(mapper::entityToApi)
                .map(item -> {
                    return new RecommendationDto(
                            item.getProductId(),
                            item.getRecommendationId(),
                            item.getAuthor(),
                            item.getRate(),
                            item.getContent(),
                            serviceUtil.getServiceAddress());
                });
    }

    @Override
    public RecommendationDto createRecommendation(RecommendationDto body) {
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<RecommendationDto> newEntity = repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity.block();
    }

    @Override
    public void deleteRecommendations(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        LOGGER.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId)).block();
    }
}
