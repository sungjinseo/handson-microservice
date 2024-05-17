package dev.greatseo.productcompositeservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.recommendation.RecommendationDto;
import dev.greatseo.api.core.review.ReviewDto;
import dev.greatseo.api.composite.ProductCompositeService;
import dev.greatseo.api.composite.product.ProductAggregate;
import dev.greatseo.util.exceptions.NotFoundException;
import dev.greatseo.util.http.ServiceUtil;
import reactor.core.publisher.Flux;
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


    /**
     * Sample usage: curl $HOST:$PORT/product-composite/1
     *
     * @param productId
     * @return the composite product info, if found, else null
     */
    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {

//        Mono<ResponseEntity<ProductDto>> productDto = integration.getProduct(productId);
//
//        if (productDto == null) throw new NotFoundException("No product found for productId: " + productId);
//
//        Flux<RecommendationDto> recommendationDtos = integration.getRecommendations(productId);
//
//        List<ReviewDto> reviewDtos = integration.getReviews(productId);
//
//        return createProductAggregate(productDto.getClass(), recommendationDtos, reviewDtos, serviceUtil.getServiceAddress());

        return Mono.zip(
                    values -> createProductAggregate(
                            (ProductDto) values[0]
                            , (List<RecommendationDto>) values[1], (List<ReviewDto>) values[2]
                            , serviceUtil.getServiceAddress()
                    ),
                    integration.getProduct(productId),
                    integration.getRecommendations(productId).collectList(),
                    integration.getReviews(productId).collectList())

                .doOnError(ex -> LOGGER.warn("getCompositeProduct failed: {}", ex.toString()))
                .log();
    }

    private ProductAggregate createProductAggregate(ProductDto productDto, List<RecommendationDto> recommendationDtos, List<ReviewDto> reviewDtos, String serviceAddress) {

        // 1. Setup product info
        int productId = productDto.productId();
        String name = productDto.name();
        int weight = productDto.weight();

        // 2. Copy summary recommendation info, if available
        List<ProductAggregate.RecommendationSummary> recommendationSummaries = (recommendationDtos == null) ? null :
                recommendationDtos.stream()
                        .map(r -> new ProductAggregate.RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ProductAggregate.ReviewSummary> reviewSummaries = (reviewDtos == null)  ? null :
                reviewDtos.stream()
                        .map(r -> new ProductAggregate.ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = productDto.serviceAddress();
        String reviewAddress = (reviewDtos != null && reviewDtos.size() > 0) ? reviewDtos.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendationDtos != null && recommendationDtos.size() > 0) ? recommendationDtos.get(0).getServiceAddress() : "";
        ProductAggregate.ServiceAddresses serviceAddresses = new ProductAggregate.ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
