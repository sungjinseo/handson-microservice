package dev.greatseo.productcompositeservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.product.ProductService;
import dev.greatseo.api.core.recommendation.RecommendationDto;
import dev.greatseo.api.core.recommendation.RecommendationService;
import dev.greatseo.api.core.review.ReviewDto;
import dev.greatseo.api.core.review.ReviewService;
import dev.greatseo.api.event.Event;
import dev.greatseo.util.exceptions.InvalidInputException;
import dev.greatseo.util.exceptions.NotFoundException;
import dev.greatseo.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatusCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static reactor.core.publisher.Flux.empty;

@Component
@CrossOrigin("*")
@RestController // 통합테스트에서 각 모듈에 대한 document를 작성하기 위한 어노테이션
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objMapper;
    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;

    private static final String PRODUCT_PUBLISH = "product-out-0";
    private static final String RECOMMENDATION_PUBLISH = "recommendation-out-0";
    private static final String REVIEWS_PUBLISH = "review-out-0";


    private final String productServiceUrl = "http://product:8080/";
    private final String recommendationServiceUrl = "http://recommendation/";
    private final String reviewServiceUrl = "http://review/";;

    @Autowired
    public ProductCompositeIntegration(@Qualifier("publishEventScheduler") Scheduler publishEventScheduler, WebClient.Builder webClientBuilder, ObjectMapper objMapper, StreamBridge streamBridge
    ) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClientBuilder = webClientBuilder;
        this.objMapper = objMapper;
        this.streamBridge = streamBridge;
    }

    private void sendMessage(String bindingName, Event event) {
        LOGGER.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
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
        return Mono.fromCallable(() -> {
            sendMessage(PRODUCT_PUBLISH, new Event(Event.Type.CREATE, body.productId(), body));
            return body;
        })
                .subscribeOn(publishEventScheduler);
//        try {
//
//            ObjectMapper mapper = new ObjectMapper();
//            final String messageKey = UUID.randomUUID().toString();
//            streamBridge.send(PRODUCT_PUBLISH
//                    , MessageBuilder
//                            .withPayload(new Event(Event.Type.CREATE, body.productId(), mapper.writeValueAsString(body)))
//                            .setHeader("MESSAGE_KEY", messageKey)
//                            .build());
//            LOGGER.info("Publish the product by msgKey test value: {}", messageKey);
//            return body;
//
//        } catch (HttpClientErrorException ex) {
//            throw handleHttpClientException(ex);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * @param productId
     * @return
     */
    @Override
    public Mono<ProductDto> getProduct(int productId) {

        try {
            String url = productServiceUrl + "product/"+ productId;
            LOGGER.debug("Will call the getProduct API on URL: {}", url);

            return this.getWebClient()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(ProductDto.class)
                    .log()
                    .onErrorMap(WebClientResponseException.class, this::handleException);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);

        }
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
        return Mono.fromRunnable(() -> {
            sendMessage("products-out-0", new Event(Event.Type.DELETE, productId, null));
        })
                .subscribeOn(publishEventScheduler)
                .then();
    }

    /**
     * Sample usage: curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId
     * @return
     */
    @Override
    public Flux<RecommendationDto> getRecommendations(int productId) {
        String url = recommendationServiceUrl  + "recommendation?productId="+ productId;

        return this.getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(RecommendationDto.class).log()
                .onErrorResume(error->empty());
    }

    @Override
    public RecommendationDto createRecommendation(RecommendationDto body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            final String messageKey = UUID.randomUUID().toString();

            LOGGER.info(mapper.writeValueAsString(body));

            streamBridge.send(RECOMMENDATION_PUBLISH
                    , MessageBuilder
                            .withPayload(new Event(Event.Type.CREATE, body.productId(), mapper.writeValueAsString(body)))
                            .setHeader("MESSAGE_KEY", messageKey)
                            .build());
            LOGGER.info("Publish the recommendation by msgKey test value: {}", messageKey);
            return body;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            final String messageKey = UUID.randomUUID().toString();
            streamBridge.send(RECOMMENDATION_PUBLISH
                    , MessageBuilder
                            .withPayload(new Event(Event.Type.DELETE, productId, null))
                            .setHeader("MESSAGE_KEY", messageKey)
                            .build());
            LOGGER.info("Publish the recommendation by msgKey test value: {}", messageKey);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    /**
     * Sample usage: curl $HOST:$PORT/review?productId=1
     *
     * @param productId
     * @return
     */
    @Override
    public Flux<ReviewDto> getReviews(int productId) {
        String url = reviewServiceUrl + "/review?productId=" + productId;

        LOGGER.debug("Will call the getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return this.getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(ReviewDto.class).log()
                .onErrorResume(error -> empty());
    }

    @Override
    public ReviewDto createReview(ReviewDto body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            final String messageKey = UUID.randomUUID().toString();
            streamBridge.send(REVIEWS_PUBLISH
                    , MessageBuilder
                            .withPayload(new Event(Event.Type.CREATE, body.productId(), mapper.writeValueAsString(body)))
                            .setHeader("MESSAGE_KEY", messageKey)
                            .build());
            LOGGER.info("Publish the review by msgKey test value: {}", messageKey);
            return body;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteReviews(int productId) {
        try {
            final String messageKey = UUID.randomUUID().toString();
            streamBridge.send(REVIEWS_PUBLISH
                    , MessageBuilder
                            .withPayload(new Event(Event.Type.DELETE, productId, null))
                            .setHeader("MESSAGE_KEY", messageKey)
                            .build());
            LOGGER.info("Publish the recommendation by msgKey test value: {}", messageKey);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {

        final HttpStatusCode statusCode = ex.getStatusCode();
        if (statusCode.equals(NOT_FOUND)) {
            throw new NotFoundException(getErrorMessage(ex));

        } else if (statusCode.equals(UNPROCESSABLE_ENTITY)) {
            throw new InvalidInputException(getErrorMessage(ex));

        }else{
            LOGGER.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
            LOGGER.warn("Error body: {}", ex.getResponseBodyAsString());
            throw ex;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOGGER.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;
        HttpStatusCode statusCode = wcre.getStatusCode();

        if (statusCode.equals(NOT_FOUND)) {
            return new NotFoundException(getErrorMessage(wcre));
        } else if (statusCode.equals(UNPROCESSABLE_ENTITY)) {
            return new InvalidInputException(getErrorMessage(wcre));
        }
        LOGGER.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
        LOGGER.warn("Error body: {}", wcre.getResponseBodyAsString());
        return ex;
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return objMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }
}
