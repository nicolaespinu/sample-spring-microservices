package com.spinic.microservices.api.core.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

    /**
     * Sample usage: "curl $HOST:$PORT/product/1".
     *
     * @param productId Id of the product
     * @return the product, if found, else null
     */
    @GetMapping(
        value = "/product/{productId}",
        produces = "application/json")
    Mono<Product> getProduct(@PathVariable int productId);


    Mono<Product> createProduct(@RequestBody Product body);


    Mono<Void> deleteProduct(@PathVariable int productId);
}
