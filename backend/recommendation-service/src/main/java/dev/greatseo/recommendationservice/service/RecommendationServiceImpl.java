package dev.greatseo.recommendationservice.service;

import dev.greatseo.api.core.recommendation.RecommendationDto;
import dev.greatseo.api.core.recommendation.RecommendationService;
import dev.greatseo.recommendationservice.repository.RecommendationRepository;
import dev.greatseo.util.exceptions.InvalidInputException;
import dev.greatseo.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

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
}
