package dev.greatseo.productcompositeservice.service;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.productcompositeservice.config.RestDocsConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import reactor.core.publisher.Mono;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

@WebMvcTest(ProductCompositeIntegration.class)
class ProductCompositeIntegrationTest extends RestDocsConfiguration {

    private static final String DEFAULT_PRODUCT = "/product/";

    @MockBean
    ProductCompositeIntegration test;

    @Test
    @DisplayName("NonReactive-테스트1. 상품 등록하기")
    void getProduct() throws Exception {

        int productId = 1;
        String uri = DEFAULT_PRODUCT + "/{productId}";

        given(test.getProduct(anyInt()))
                .willReturn(Mono.just(new ProductDto(productId,"",1,"")));

        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.get(uri, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(
                MockMvcRestDocumentationWrapper.document(
                        "product-docs",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .description("description")
                                        .pathParameters(
                                                parameterWithName("productId").description("사용자 id")
                                        )
                                        .responseFields(
                                                fieldWithPath("productId").description("상품ID"),
                                                fieldWithPath("name").description("상품명"),
                                                fieldWithPath("weight").description("무게"),
                                                fieldWithPath("serviceAddress").description("서비스주소")
                                        )
                                        .build()
                        ))
        );

        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }
}