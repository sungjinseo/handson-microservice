package dev.greatseo.reviewservice.service;

import dev.greatseo.api.core.review.ReviewDto;
import dev.greatseo.reviewservice.repository.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    ReviewDto entityToApi(ReviewEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    ReviewEntity apiToEntity(ReviewDto api);

    List<ReviewDto> entityListToApiList(List<ReviewEntity> entity);
    List<ReviewEntity> apiListToEntityList(List<ReviewDto> api);
}