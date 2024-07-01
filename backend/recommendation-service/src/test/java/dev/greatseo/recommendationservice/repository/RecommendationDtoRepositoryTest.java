package dev.greatseo.recommendationservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class RecommendationDtoRepositoryTest {

    @Autowired
    private RecommendationRepository repository;

    private RecommendationEntity savedEntity;

    @BeforeEach
    public void setupDb() {
        repository.deleteAll().block();
        savedEntity = new RecommendationEntity(1, 2, "a", 3, "c");
        Mono<RecommendationEntity> saveProduct = repository.save(savedEntity);
        StepVerifier.create(saveProduct)
                .assertNext(product->assertEqualsRecommendation(product, savedEntity))
                .expectComplete()
                .verify();

    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(),           actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(),           actualEntity.getRating());
        assertEquals(expectedEntity.getContent(),          actualEntity.getContent());
    }

    private boolean areRecommendationEqual(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        return
                (expectedEntity.getId().equals(actualEntity.getId())) &&
                        (expectedEntity.getVersion().equals(actualEntity.getVersion())) &&
                        (expectedEntity.getProductId() == actualEntity.getProductId()) &&
                        (expectedEntity.getAuthor().equals(actualEntity.getAuthor())) &&
                        (expectedEntity.getContent().equals(actualEntity.getContent())) &&
                        (expectedEntity.getRecommendationId() == actualEntity.getRecommendationId());
    }

    @Test
    @DisplayName("Reactive-테스트1. 추천 등록하기")
    void TEST1_CREATE() {

        RecommendationEntity newEntity = new RecommendationEntity(1, 3, "a", 3, "c");
        repository.save(newEntity).block();

        RecommendationEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    @Test
    @DisplayName("Reactive-테스트2. 추천 저장하기")
    void TEST2_UPDATE() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity).block();

        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    @DisplayName("Reactive-테스트3. 추천 삭제하기")
    public void TEST3_DELETE() {
        repository.delete(savedEntity).block();
        assertNotEquals(Boolean.TRUE, repository.existsById(savedEntity.getId()).block());
    }

    @Test
    @DisplayName("Reactive-테스트4. 추천정보 조회하기 다건이 될 수 있음")
    public void TEST4_SELECT_BY_PRODUCT_ID() {
        StepVerifier.create(
                repository.findByProductId(savedEntity.getProductId())
                )
                .expectNextMatches(foundEntity -> areRecommendationEqual(savedEntity, foundEntity))
                .verifyComplete();
//        Flux<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId());
//
//        //assertEquals(entityList.collectList().block(), hasSize(1));
//        assertEquals(entityList.count().block(), 1);
//        assertEqualsRecommendation(savedEntity, entityList.blockFirst());

    }

    @Test
    @DisplayName("Reactive-테스트5. 인덱스 중복 에러")
    public void TEST5_duplicateError() {
        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
        assertThrows(DuplicateKeyException.class, ()->{
            repository.save(entity).block();
        });
    }

    @Test
    @DisplayName("Reactive-테스트6. 낙관적 락 에러 검증")
    public void TEST6_optimisticLockError() {

        // Store the saved entity in two separate entity objects
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).block();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setAuthor("a2");
            repository.save(entity2).block();

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }
}