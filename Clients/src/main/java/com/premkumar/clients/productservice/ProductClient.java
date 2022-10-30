package com.premkumar.clients.productservice;

import com.premkumar.clients.config.FeignConfig;
import com.premkumar.clients.exception.CustomException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "productservice/products", configuration = FeignConfig.class)
public interface ProductClient {


    @PutMapping("/reduceQuantity/{id}")
    ResponseEntity<Void> reduceQuantity(
            @PathVariable("id") long productId,
            @RequestParam("quantity") long quantity
    );

    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> getProductById(@PathVariable("id") long productId);

    default ResponseEntity<Long> fallback(Exception e) {
        throw new CustomException("Product Service is not available",
                "UNAVAILABLE",
                500);
    }
}
