package dev.greatseo.api.core.recommendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.util.List;

public interface RecommendationService {
    /**
     * Sample usage: curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId
     * @return
     */
    @GetMapping(
            value    = "/recommendation",
            produces = "application/json")
    Flux<RecommendationDto> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

    RecommendationDto createRecommendation(@RequestBody RecommendationDto body);

    void deleteRecommendations(@RequestParam(value = "productId", required = true)  int productId);
}
