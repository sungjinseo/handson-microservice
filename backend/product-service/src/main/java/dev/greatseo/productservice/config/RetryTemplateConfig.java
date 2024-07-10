package dev.greatseo.productservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamRetryTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryTemplateConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(RetryTemplateConfig.class);
    private final int MAX_RETRY_ATTEMPTS = 2;

    //에러 발생에 대한 리스너를 구현합니다. (재시도 시작 혹은 에러발생시에 대해서 처리가능)
    private final RetryListener listener = new RetryListener() {
        @Override
        public <T, E extends Throwable> void onError(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {
            LOGGER.info("=========== RETRY ERROR {} 번째 발생 ================", retryContext.getRetryCount());
            LOGGER.info("Retry Exception Trace::: ", throwable.getCause());
        }
    };

    @StreamRetryTemplate
    public RetryTemplate defaultRetryTemplate(){
        return RetryTemplate.builder()
                .maxAttempts(MAX_RETRY_ATTEMPTS)
                .exponentialBackoff(5000, 2.0, 30000)
                .withListener(listener)
                .build();
    }

}