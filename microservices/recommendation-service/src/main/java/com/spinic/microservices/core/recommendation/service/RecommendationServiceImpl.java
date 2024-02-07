package com.spinic.microservices.core.recommendation.service;

import com.spinic.microservices.api.core.recommendation.Recommendation;
import com.spinic.microservices.api.core.recommendation.RecommendationService;
import com.spinic.microservices.api.exceptions.InvalidInputException;
import com.spinic.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " +productId);
        }

        if (productId ==113) {
            LOGGER.debug("No recommendations found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Recommendation> recommendationList = new ArrayList<>();
        recommendationList.add(new Recommendation(productId, 1, "Author_#-1", 1, "Content 1", serviceUtil.getServiceAddress()));
        recommendationList.add(new Recommendation(productId, 2, "Author_#-2", 2, "Content 2", serviceUtil.getServiceAddress()));
        recommendationList.add(new Recommendation(productId, 3, "Author_#-3", 3, "Content 3", serviceUtil.getServiceAddress()));

        LOGGER.debug("/recommendation response size: {}", recommendationList.size());
        return recommendationList;
    }
}
