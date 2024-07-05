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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static reactor.core.publisher.Flux.empty;

@Component
@CrossOrigin("*")
@RestController // 통합테스트에서 각 모듈에 대한 document를 작성하기 위한 어노테이션
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private static final String HTTP_BEGIN = "http://";
    private static final String HTTPS_BEGIN = "https://";


    private final RestTemplate restTemplate;
    private final ObjectMapper objMapper;
    private final WebClient webClient;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;
    private final StreamBridge streamBridge;

    private static final String PRODUCTS_PUBLISH = "products-out-0";

    @Autowired
    public ProductCompositeIntegration(WebClient.Builder webClient, ObjectMapper objMapper,
                                       @Value("${app.product-service.host}") String productServiceHost,
                                       @Value("${app.product-service.port}") int    productServicePort,

                                       @Value("${app.recommendation-service.host}") String recommendationServiceHost,
                                       @Value("${app.recommendation-service.port}") int    recommendationServicePort,

                                       @Value("${app.review-service.host}") String reviewServiceHost,
                                       @Value("${app.review-service.port}") int    reviewServicePort,

                                       RestTemplateBuilder restBuilder,
                                       StreamBridge streamBridge
    ) {
        this.restTemplate = restBuilder.build();
        this.webClient = webClient.build();
        this.objMapper = objMapper;
        this.streamBridge = streamBridge;

        productServiceUrl        = HTTP_BEGIN + productServiceHost + ":" + productServicePort + "/product/";
        recommendationServiceUrl = HTTP_BEGIN + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        reviewServiceUrl         = HTTP_BEGIN + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
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
    public ProductDto createProduct(ProductDto body) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            final String messageKey = UUID.randomUUID().toString();
            streamBridge.send(PRODUCTS_PUBLISH
                    , MessageBuilder
                            .withPayload(new Event(Event.Type.CREATE, body.productId(), mapper.writeValueAsString(body)))
                            .setHeader("MESSAGE_KEY", messageKey)
                            .build());
            LOGGER.info("Publish the product by msgKey test value: {}", messageKey);
            return body;
            //return ResponseEntity.created(URI.create("/product/" + body.productId())).build();

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param productId
     * @return
     */
    @Override
    public Mono<ProductDto> getProduct(int productId) {

        try {
            String url = productServiceUrl + productId;
            LOGGER.debug("Will call the getProduct API on URL: {}", url);

            return webClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(ProductDto.class)
                    .log()
                    .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));

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
    public ResponseEntity deleteProduct(int productId) {
        return ResponseEntity.noContent().build();
    }

    /**
     * Sample usage: curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId
     * @return
     */
    @Override
    public Flux<RecommendationDto> getRecommendations(int productId) {

        String url = recommendationServiceUrl + productId;

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(RecommendationDto.class).log()
                .onErrorResume(error->empty());

        /**
         * blocking code
         */
//        try {
//            String url = recommendationServiceUrl + productId;
//
//            LOGGER.debug("Will call getRecommendations API on URL: {}", url);
//            List<RecommendationDto> recommendationDtos = restTemplate.exchange(
//                    url,
//                    GET,
//                    null,
//                    new ParameterizedTypeReference<List<RecommendationDto>>() {}
//            ).getBody();
//
//            LOGGER.debug("Found {} recommendations for a product with id: {}", recommendationDtos.size(), productId);
//            return Flux.fromIterable(recommendationDtos);
//
//        } catch (Exception ex) {
//            LOGGER.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
//            return Flux.just();
//        }
    }

    /**
     * Sample usage: curl $HOST:$PORT/review?productId=1
     *
     * @param productId
     * @return
     */
    @Override
    public Flux<ReviewDto> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;

            LOGGER.debug("Will call getReviews API on URL: {}", url);
            List<ReviewDto> reviewDtos = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<ReviewDto>>() {}).getBody();

            LOGGER.debug("Found {} reviews for a product with id: {}", reviewDtos.size(), productId);
            //return reviewDtos;
            return Flux.fromIterable(reviewDtos);

        } catch (Exception ex) {
            LOGGER.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return Flux.just();
            //return new ArrayList<>();
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
}
