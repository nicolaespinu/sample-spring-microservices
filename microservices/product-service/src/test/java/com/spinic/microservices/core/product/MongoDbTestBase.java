package com.spinic.microservices.core.product;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

public abstract class MongoDbTestBase {

    private static MongoDBContainer dbContainer = new MongoDBContainer("mongo:6.0.4");

    static {
        dbContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", dbContainer::getContainerIpAddress);
        registry.add("spring.data.mongodb.port", () -> dbContainer.getMappedPort(27017));
        registry.add("spring.data.mongodb.database", () -> "test");
    }
}
