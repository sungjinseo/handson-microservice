package dev.greatseo.reviewservice.service;

import dev.greatseo.api.core.review.ReviewDto;
import dev.greatseo.api.core.review.ReviewService;
import dev.greatseo.reviewservice.repository.ReviewEntity;
import dev.greatseo.reviewservice.repository.ReviewRepository;
import dev.greatseo.util.exceptions.InvalidInputException;
import dev.greatseo.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final Scheduler scheduler;

    private final ServiceUtil serviceUtil;

    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(Scheduler scheduler, ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper) {
        this.scheduler = scheduler;
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<ReviewDto> getReviews(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return asyncFlux(getByProductId(productId));
    }

    protected List<ReviewDto> getByProductId(int productId){
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<ReviewDto> reviewDtoList = mapper.entityListToApiList(entityList);

        // 객체의 변화를 주는 것이 올바른가...
        reviewDtoList.forEach(item ->{
            item.setServiceAddress(serviceUtil.getServiceAddress());
        });

        LOGGER.info("getReview: response size: {}", reviewDtoList.size());

        return reviewDtoList;
    }

    private <T> Flux<T> asyncFlux(Iterable<T> iterable) {
        return Flux.fromIterable(iterable).publishOn(scheduler);
    }
}
