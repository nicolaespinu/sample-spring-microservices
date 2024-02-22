package com.spinic.microservices.core.product.services;

import com.mongodb.DuplicateKeyException;
import com.spinic.microservices.api.core.product.Product;
import com.spinic.microservices.api.core.product.ProductService;
import com.spinic.microservices.api.exceptions.InvalidInputException;
import com.spinic.microservices.api.exceptions.NotFoundException;
import com.spinic.microservices.core.product.persistence.ProductEntity;
import com.spinic.microservices.core.product.persistence.ProductRepository;
import com.spinic.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Product getProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOGGER.debug("getProduct: found productId: {}", response.getProductId());

        return response;
    }

    public Product createProduct(Product body) {
        try {
            ProductEntity entity = mapper.apiToEntity(body);
            ProductEntity newEntity = repository.save(entity);

            LOGGER.debug("createProduct: entity created for productId: {}", body.getProductId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
        }
    }

    public void deleteProduct(int productId) {
        LOGGER.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
    }
}

