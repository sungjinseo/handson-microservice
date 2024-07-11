package dev.greatseo.api.core.product;

import lombok.Builder;

@Builder
public record ProductDto(int productId, String name, int weight, String serviceAddress) {
}