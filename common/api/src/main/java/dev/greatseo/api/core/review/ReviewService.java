package dev.greatseo.api.core.review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ReviewService {

    /**
     * Sample usage: curl $HOST:$PORT/review?productId=1
     *
     * @param productId
     * @return
     */
    @GetMapping(
            value    = "/review",
            produces = "application/json")
    Flux<ReviewDto> getReviews(@RequestParam(value = "productId", required = true) int productId);
}