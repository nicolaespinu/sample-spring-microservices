package com.spinic.microservices.api.composite.product;

public class ReviewSummary {
    private final int reviewId;
    private final String author;
    private final int subject;

    public ReviewSummary(int reviewId, String author, int subject) {
        this.reviewId = reviewId;
        this.author = author;
        this.subject = subject;
    }

    public int getReviewId() {
        return reviewId;
    }

    public String getAuthor() {
        return author;
    }

    public int getSubject() {
        return subject;
    }
}
