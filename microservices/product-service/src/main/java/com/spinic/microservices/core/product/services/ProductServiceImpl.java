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
import reactor.core.publisher.Mono;

import java.util.logging.Level;

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
    public Mono<Product> getProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOGGER.info("Will get product info for id={}", productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Not product found for productId: " + productId)))
                .log(LOGGER.getName(), Level.FINE)
                .map(productEntity -> mapper.entityToApi(productEntity))
                .map(entity -> setServiceAddress(entity));
    }

    public Mono<Product> createProduct(Product body) {

        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid product ID: " + body.getProductId());
        }

        ProductEntity productEntity = mapper.apiToEntity(body);
        Mono<Product> newEntity = repository.save(productEntity)
                .log(LOGGER.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        e -> new InvalidInputException("Duplicate Key, Product ID: " + body.getProductId()))
                .map(entity -> mapper.entityToApi(entity));
        return newEntity;
    }

    public Mono<Void> deleteProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid product ID: " + productId);
        }

        LOGGER.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId)
                .log(LOGGER.getName(), Level.FINE)
                .map(entity -> repository.delete(entity))
                .flatMap(entity -> entity);
    }

    private Product setServiceAddress(Product entity) {
        entity.setServiceAddress(serviceUtil.getServiceAddress());
        return entity;
    }
}

