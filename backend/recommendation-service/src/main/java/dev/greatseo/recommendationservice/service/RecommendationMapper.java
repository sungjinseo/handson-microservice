package dev.greatseo.recommendationservice.service;

import dev.greatseo.api.core.recommendation.RecommendationDto;
import dev.greatseo.recommendationservice.repository.RecommendationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mappings({
            @Mapping(target = "rate", source="entity.rating"),
            @Mapping(target = "serviceAddress", ignore = true)
    })
    RecommendationDto entityToApi(RecommendationEntity entity);

    @Mappings({
            @Mapping(target = "rating", source="api.rate"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    RecommendationEntity apiToEntity(RecommendationDto api);

    List<RecommendationDto> entityListToApiList(List<RecommendationEntity> entity);
    List<RecommendationEntity> apiListToEntityList(List<RecommendationDto> api);
}