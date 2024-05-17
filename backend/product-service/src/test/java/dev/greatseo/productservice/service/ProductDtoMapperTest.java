package dev.greatseo.productservice.service;

import dev.greatseo.api.core.product.ProductDto;
import dev.greatseo.productservice.repository.ProductEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDtoMapperTest {
    private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    public void mapperTests() {

        assertNotNull(mapper);
        ProductDto api = new ProductDto(1, "n", 1, "sa");

        ProductEntity entity = mapper.apiToEntity(api);

        assertEquals(api.productId(), entity.getProductId());
        assertEquals(api.name(), entity.getName());
        assertEquals(api.weight(), entity.getWeight());

        ProductDto api2 = mapper.entityToApi(entity);

        assertEquals(api.productId(), api2.productId());
        assertEquals(api.name(),      api2.name());
        assertEquals(api.weight(),    api2.weight());
        assertNull(api2.serviceAddress());
    }
}
