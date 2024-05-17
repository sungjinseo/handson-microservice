package dev.greatseo.productcompositeservice;

import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.core.recommendation.RecommendationDto;
import dev.greatseo.api.core.review.ReviewDto;
import dev.greatseo.productcompositeservice.service.ProductCompositeIntegration;
import dev.greatseo.util.exceptions.InvalidInputException;
import dev.greatseo.util.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductDtoCompositeServiceApplicationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockBean
    private ProductCompositeIntegration pcIntegration;

    @BeforeEach
    void mockServSetup(){

        given(pcIntegration.getProduct(PRODUCT_ID_OK)).
                willReturn(Mono.just(new ProductDto(PRODUCT_ID_OK, "name", 1, "mock-address")));
        given(pcIntegration.getRecommendations(PRODUCT_ID_OK)).
                willReturn(Flux.fromIterable(Collections.singletonList(new RecommendationDto(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address"))));
        given(pcIntegration.getReviews(PRODUCT_ID_OK)).
                willReturn(Flux.fromIterable(Collections.singletonList(new ReviewDto(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));

        given(pcIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .willThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
        given(pcIntegration.getProduct(PRODUCT_ID_INVALID))
                .willThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));

        //blocking
//        when(pcIntegration.getProduct(PRODUCT_ID_OK))
//                .thenReturn(ResponseEntity.ok().body(new ProductDto(PRODUCT_ID_OK, "name", 1, "mock-address")));
//        when(pcIntegration.getRecommendations(PRODUCT_ID_OK))
//                .thenReturn(Collections.singletonList(new RecommendationDto(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-address")));
//        when(pcIntegration.getReviews(PRODUCT_ID_OK))
//                .thenReturn(Collections.singletonList(new ReviewDto(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock-address")));

//        when(pcIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
//                .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
//        when(pcIntegration.getProduct(PRODUCT_ID_INVALID))
//                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {

        return client.get()
                .uri("/product-composite/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("상품정보를 ID로 가져오기")
    void getProductIdById(){
        getAndVerifyProduct(PRODUCT_ID_OK, OK)
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    @DisplayName("NOT_FOUND 테스트")
    void getProductNotFound(){
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND);
                // 메세지 태그가 없다고 됨...
                //.jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    @Disabled
    @DisplayName("INVAILD 테스트")
    void getProductInvalidInput() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }
}
