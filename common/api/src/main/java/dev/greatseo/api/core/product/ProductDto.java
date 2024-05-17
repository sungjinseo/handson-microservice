package dev.greatseo.api.core.product;

import lombok.Builder;

@Builder
public record ProductDto(int productId, String name, int weight, String serviceAddress) {
//    private int productId;
//    private String name;
//    private int weight;
//    private String serviceAddress;
//
//    public ProductDto(int productId, String name, int weight, String serviceAddress) {
//        this.productId = productId;
//        this.name = name;
//        this.weight = weight;
//        this.serviceAddress = serviceAddress;
//    }
//
//    public int getProductId() {
//        return productId;
//    }
//
//    public String getName() {
//        return name;
//    }
//    public int getWeight() {
//        return weight;
//    }
//    public String getServiceAddress() {
//        return serviceAddress;
//    }

}