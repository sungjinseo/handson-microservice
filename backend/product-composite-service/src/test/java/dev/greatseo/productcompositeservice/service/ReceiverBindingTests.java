package dev.greatseo.productcompositeservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.api.event.Event;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.function.StreamBridge;

import java.io.IOException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ReceiverBindingTests {

    @Autowired
    private InputDestination inputDestination;
    @Autowired
    private OutputDestination outputDestination;
    @Autowired
    private StreamBridge streamBridge;
    @MockBean
    private Function<Event, ProductDto> receiverMessageHandler;

    @Disabled
    @Test
    void consumeMessageTest() {
        // payload 구성
        ProductDto payload = new ProductDto(1, "test",1,"");

        // receiverMessageHandler mocking
        //given(receiverMessageHandler.apply(any(Event.class)))
        //        .willReturn(new ProductDto(1, "test",1,"");

        // MessageChannel에 메시지가 들어온 상태 생성
        //inputDestination.send(MessageBuilder.withPayload(payload).build());

        // receiverMessageHandler 호출 검증
        //verify(receiverMessageHandler).apply(payload);

    }
    @Disabled
    @Test
    void publishMessageTest() throws IOException {
        // payload 구성
        ProductDto payload = new ProductDto(1, "test",1,"");

        // 메시지 발행
        streamBridge.send("product-out-0", payload);

        // Message 확인
        byte[] received = outputDestination.receive(10L, "test2").getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        ProductDto receivedPayload = objectMapper.reader().readValue(received, ProductDto.class);
        assertThat(receivedPayload).isEqualTo(payload);
    }
}