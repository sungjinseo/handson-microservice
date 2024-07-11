package dev.greatseo.productcompositeservice.service;

import dev.greatseo.api.composite.ProductCompositeService;
import dev.greatseo.api.composite.product.ProductAggregate;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.recommendation.RecommendationDto;
import dev.greatseo.api.core.review.ReviewDto;
import dev.greatseo.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {
        return Mono.zip(
                    values -> createProductAggregate(
                              (ProductDto) values[0]
                            , (List<RecommendationDto>) values[1]
                            , (List<ReviewDto>) values[2]
                            , serviceUtil.getServiceAddress()
                    ),
                    integration.getProduct(productId),
                    integration.getRecommendations(productId).collectList(),
                    integration.getReviews(productId).collectList())

                .doOnError(ex -> LOGGER.warn("getCompositeProduct failed: {}", ex.toString()))
                .log();
    }

    @Override
    public void createCompositeProduct(ProductAggregate body) {

        try {

            LOGGER.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            ProductDto product = new ProductDto(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    RecommendationDto recommendation = new RecommendationDto(body.getProductId(), r.recommendationId(), r.author(), r.rate(), r.content(), null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    ReviewDto review = new ReviewDto(body.getProductId(), r.reviewId(), r.author(), r.subject(), r.content(), null);
                    integration.createReview(review);
                });
            }

            LOGGER.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            LOGGER.warn("createCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    @Override
    public void deleteCompositeProduct(int productId) {

    }

    private ProductAggregate createProductAggregate(ProductDto productDto, List<RecommendationDto> recommendationDtoList, List<ReviewDto> reviewDtoList, String serviceAddress) {

        // 1. Setup product info
        int productId = productDto.productId();
        String name = productDto.name();
        int weight = productDto.weight();

        // 2. Copy summary recommendation info, if available
        List<ProductAggregate.RecommendationSummary> recommendationSummaries = (recommendationDtoList == null) ? null :
                recommendationDtoList.stream()
                        .map(r -> new ProductAggregate.RecommendationSummary(r.recommendationId(), r.author(), r.rate(), r.content()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ProductAggregate.ReviewSummary> reviewSummaries = (reviewDtoList == null)  ? null :
                reviewDtoList.stream()
                        .map(r -> new ProductAggregate.ReviewSummary(r.reviewId(), r.author(), r.subject(), r.content()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = productDto.serviceAddress();
        String reviewAddress = (reviewDtoList != null && !reviewDtoList.isEmpty()) ? reviewDtoList.get(0).serviceAddress() : "";
        String recommendationAddress = (recommendationDtoList != null && !recommendationDtoList.isEmpty()) ? recommendationDtoList.get(0).serviceAddress() : "";
        ProductAggregate.ServiceAddresses serviceAddresses = new ProductAggregate.ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
