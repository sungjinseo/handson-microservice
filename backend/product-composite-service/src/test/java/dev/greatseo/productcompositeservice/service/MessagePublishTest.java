package dev.greatseo.productcompositeservice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.event.Event;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
@Import(TestChannelBinderConfiguration.class)
public class MessagePublishTest {

    private static final String PRODUCTS_PUBLISH = "products-out-0";

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private OutputDestination outputDestination;

    @Disabled
    @Test
    @DisplayName("1. 프로덕트 퍼블리시 테스트")
    void productPublishMessageTest() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final String messageKey = UUID.randomUUID().toString();

        // payload 구성
        ProductDto createProduct = new ProductDto(1,"name",1,"");
        Event payload = new Event(Event.Type.CREATE, createProduct.productId(), mapper.writeValueAsString(createProduct));

        streamBridge.send(PRODUCTS_PUBLISH
                , MessageBuilder
                        .withPayload(payload)
                        .setHeader("MESSAGE_KEY", messageKey)
                        .build());

        // message 확인
        byte[] received = outputDestination.receive().getPayload();
        //MessageHeaders msgHeader  = outputDestination.receive().getHeaders();
        Event receivedPayload = mapper.readValue(received, Event.class);
        ProductDto item = mapper.readValue(receivedPayload.getValue().toString(), ProductDto.class);
        assertThat(receivedPayload.getEventType()).isEqualTo(Event.Type.CREATE);
        assertThat(item).isEqualTo(createProduct);
    }
}
