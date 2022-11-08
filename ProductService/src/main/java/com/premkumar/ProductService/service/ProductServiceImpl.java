package com.premkumar.ProductService.service;

import com.premkumar.ProductService.entity.Product;
import com.premkumar.ProductService.exception.ProductServiceCustomException;
import com.premkumar.ProductService.model.ProductRequest;
import com.premkumar.ProductService.model.ProductResponse;
import com.premkumar.ProductService.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.beans.BeanUtils.copyProperties;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    private static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding Product..{}", productRequest);

        Product product = Product.builder()
                .productName(productRequest.getName())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice())
                .build();
        product = productRepository.save(product);

        log.info("Product Created....{}", product);
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("Get the product for productId: {}", productId);

        Product product
                = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException(
                        "Product with given Id not found",
                        PRODUCT_NOT_FOUND
                ));

        ProductResponse productResponse = new ProductResponse();
        copyProperties(product, productResponse);

        log.info("Product Response for productId: {}", productResponse);
        return productResponse;
    }

    @Override
    public void deleteProductById(long productId) {
        log.info("Product id: {}", productId);

        if (!productRepository.existsById(productId)) {
            log.info("Im in this loop {}", !productRepository.existsById(productId));
            throw new ProductServiceCustomException(
                    "Product with given with id: " + productId + " not found:",
                    PRODUCT_NOT_FOUND);
        }
        log.info("Deleting Product with id: {}", productId);
        productRepository.deleteById(productId);

    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce Quantity {} for Id: {}", quantity, productId);

        Product product
                = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException(
                        "Product with given Id not found",
                        PRODUCT_NOT_FOUND
                ));
        log.info(" Product Details:{}", product);
        if (product.getQuantity() < quantity) {
            throw new ProductServiceCustomException(
                    "Product does not have sufficient Quantity",
                    "INSUFFICIENT_QUANTITY"
            );
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Product Quantity updated Successfully");
        log.info("Updated Product Details:{}", product.getQuantity());
    }


}
