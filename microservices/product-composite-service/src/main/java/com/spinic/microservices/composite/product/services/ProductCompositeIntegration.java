package com.spinic.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spinic.microservices.api.core.product.Product;
import com.spinic.microservices.api.core.product.ProductService;
import com.spinic.microservices.api.core.recommendation.Recommendation;
import com.spinic.microservices.api.core.recommendation.RecommendationService;
import com.spinic.microservices.api.core.review.Review;
import com.spinic.microservices.api.core.review.ReviewService;
import com.spinic.microservices.api.exceptions.InvalidInputException;
import com.spinic.microservices.api.exceptions.NotFoundException;
import com.spinic.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductCompositeIntegration implements ProductService, ReviewService, RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort) {

        this.restTemplate = restTemplate;
        this.mapper = mapper;
        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort
                + "/recommendation?productId=";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";

    }

    @Override
    public Product getProduct(int productId) {

        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);
            Product product = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found a product with id: {}", product.getProductId());

            return product;
        } catch (HttpClientErrorException exception) {
            switch (HttpStatus.resolve(exception.getStatusCode().value())) {
                case NOT_FOUND -> {
                    throw new NotFoundException(getErrorMessage(exception));
                }
                case UNPROCESSABLE_ENTITY -> {
                    throw new InvalidInputException(getErrorMessage(exception));
                }
                default -> {
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
                    LOG.warn("Error body: {}", exception.getResponseBodyAsString());
                    throw exception;
                }
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException exception) {
        try {
            return mapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;
            LOG.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate
                    .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
                    })
                    .getBody();
            LOG.debug("Found recommendations for product with Id: {}", productId);
            return recommendations;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;
            LOG.debug("Will call getReview API on URL: {}", url);
            List<Review> reviewList = restTemplate
                    .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {
                    })
                    .getBody();
            LOG.debug("Found {} reviews for product with Id: {}", reviewList.size(), productId);
            return reviewList;
        } catch (Exception e) {
            LOG.warn("Got an exception while requesting reviews, returns zero reviews: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
