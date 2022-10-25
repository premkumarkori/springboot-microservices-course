package com.premkumar.ProductService.service;

import com.premkumar.ProductService.model.ProductRequest;
import com.premkumar.ProductService.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void deleteProductById(long productId);
}
