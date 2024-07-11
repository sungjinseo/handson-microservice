package dev.greatseo.reviewservice.service;

import dev.greatseo.api.core.review.ReviewDto;
import dev.greatseo.api.core.review.ReviewService;
import dev.greatseo.reviewservice.repository.ReviewEntity;
import dev.greatseo.reviewservice.repository.ReviewRepository;
import dev.greatseo.util.exceptions.InvalidInputException;
import dev.greatseo.util.http.ServiceUtil;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.logging.Level.FINE;

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
    public ReviewDto createReview(ReviewDto body) {

        if (body.productId() < 1) throw new InvalidInputException("Invalid productId: " + body.productId());

        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            LOGGER.debug("createReview: created a review entity: {}/{}", body.productId(), body.reviewId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Review Id:" + body.reviewId());
        }
    }

    @Override
    public Flux<ReviewDto> getReviews(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        LOGGER.info("Will get reviews for product with id={}", productId);

        return asyncFlux(() -> Flux.fromIterable(getByProductId(productId))).log(null, FINE);
    }

    protected List<ReviewDto> getByProductId(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        //List<ReviewDto> list = mapper.entityListToApiList(entityList);
        //LOGGER.debug("getReviews: response size: {}", list.size());
        return entityList.stream()
                .map(mapper::entityToApi).map(item-> new ReviewDto(
                        item.productId(),
                        item.reviewId(),
                        item.author(),
                        item.subject(),
                        item.content(),
                        serviceUtil.getServiceAddress())
        ).toList();
    }

    @Override
    public void deleteReviews(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        LOGGER.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }

    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}
