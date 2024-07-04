package dev.greatseo.productcompositeservice.service;


import dev.greatseo.api.core.product.ProductDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment=RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Import(TestChannelBinderConfiguration.class)
@ExtendWith(MockitoExtension.class)
public class MessageConsumeTest {

    @Autowired
    private InputDestination inputDestination;

    @MockBean
    private Consumer<ProductDto> producerConsume;

    @Captor
    private ArgumentCaptor<ProductDto> captor;

    @Test
    void consumeMessageTest() {
        // payload 구성
        ProductDto payload = new ProductDto(1,"name",1,"");

        // MessageChannel에 메시지가 들어온 상태 생성
        inputDestination.send(MessageBuilder.withPayload(payload).build(), "products-out-0");

        // listener 호출 검증
        verify(producerConsume).accept(captor.capture());

        // argument 검증
        assertThat(captor.getValue()).isEqualTo(payload);
    }
}
