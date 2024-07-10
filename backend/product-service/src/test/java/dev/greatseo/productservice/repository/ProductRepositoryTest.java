package dev.greatseo.productservice.repository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    public void setupDb() {
        repository.deleteAll().block();
        ProductEntity entity = new ProductEntity(1, "n", 1);
        savedEntity = repository.save(entity).block();
        assertEqualsProduct(entity, savedEntity);
    }

    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getName(),           actualEntity.getName());
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());
    }

    @AfterAll
    public void destroyData() {
        repository.deleteAll();
    }


    @Test
    @DisplayName("NonReactive-테스트1. 상품 등록하기")
    void TEST1_CREATE() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        repository.save(newEntity).block();

        ProductEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    @Test
    @DisplayName("NonReactive-테스트2. 상품 저장하기")
    void TEST2_UPDATE() {
        savedEntity.setName("n2");
        repository.save(savedEntity).block();

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    @DisplayName("NonReactive-테스트3. 상품 삭제하기")
    public void TEST3_DELETE() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
    @DisplayName("NonReactive-테스트4. 상품정보 단건 조회하기")
    public void TEST4_SELECT_BY_PRODUCT_ID() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId()).blockOptional();
        assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    @Test
    @DisplayName("NonReactive-테스트5. 인덱스 중복 에러")
    public void TEST5_duplicateError() {
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
        assertThrows(DuplicateKeyException.class, ()->{
            repository.save(entity).block();
        });
    }

    @Test
    @DisplayName("NonReactive-테스트6. 낙관적 락 에러 검증")
    public void TEST6_optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setName("n2");
            repository.save(entity2).block();

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

}