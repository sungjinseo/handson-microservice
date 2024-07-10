package dev.greatseo.reviewservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
@TestPropertySource(locations = "classpath:application.yml")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @BeforeEach
    public void setupDb() {
        repository.deleteAll();
        ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
        savedEntity = repository.save(entity);
        assertEqualsReview(entity, savedEntity);
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(),        actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),   actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getReviewId(),  actualEntity.getReviewId());
        assertEquals(expectedEntity.getAuthor(),    actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(),   actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(),   actualEntity.getContent());
    }

    @Test
    @DisplayName("NonReactive-테스트1. 리뷰 등록하기")
    void TEST1_CREATE() {
        ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c");
        repository.save(newEntity);

        ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsReview(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("NonReactive-테스트2. 리뷰 저장하기")
    void TEST2_UPDATE() {
        //given
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        //when
        ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();

        //then
        assertEquals(1L, foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());

    }

    @Test
    @DisplayName("NonReactive-테스트3. 리뷰 삭제하기")
    public void TEST3_DELETE() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    @DisplayName("NonReactive-테스트4. 상품정보 단건 조회하기")
    public void TEST4_SELECT_BY_PRODUCT_ID() {
        List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertThat(entityList, hasSize(1));
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test
    @DisplayName("NonReactive-테스트5. 인덱스 중복 에러")
    public void TEST5_duplicateError() {
        ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, ()->{
            repository.save(entity);
        });
        System.out.println(exception.getMessage());
    }

    @Test
    @DisplayName("NonReactive-테스트6. 낙관적 락 에러 검증")
    public void TEST6_optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

        System.out.println(entity1.getVersion());
        System.out.println(entity2.getVersion());

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        repository.save(entity1);

        System.out.println(entity1.getVersion());
        System.out.println(entity2.getVersion());

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setAuthor("a2");
            repository.save(entity2);

            System.out.println(entity1.getVersion());
            System.out.println(entity2.getVersion());

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();

        System.out.println(entity1.getVersion());
        System.out.println(entity2.getVersion());

        assertEquals(1L, updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

}