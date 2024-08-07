package dev.greatseo.api.core.review;
public class ReviewDto {
    private final int productId;
    private final int reviewId;
    private final String author;
    private final String subject;
    private final String content;
    private String serviceAddress;

    public ReviewDto(int productId, int reviewId, String author, String subject, String content, String serviceAddress) {
        this.productId = productId;
        this.reviewId = reviewId;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.serviceAddress = serviceAddress;
    }

    public int getProductId() {
        return productId;
    }
    public int getReviewId() {
        return reviewId;
    }
    public String getAuthor() {
        return author;
    }
    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }
    public String getServiceAddress() {
        return serviceAddress;
    }
    public void setServiceAddress(String serviceAddress) { this.serviceAddress =serviceAddress;}
}