package com.premkumar.ProductService.service;

import com.premkumar.ProductService.entity.Product;
import com.premkumar.ProductService.exception.ProductServiceCustomException;
import com.premkumar.ProductService.model.ProductRequest;
import com.premkumar.ProductService.model.ProductResponse;
import com.premkumar.ProductService.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static org.springframework.beans.BeanUtils.copyProperties;

@Service
@Slf4j
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding Product..");

        Product product = Product.builder()
                .productName(productRequest.getName())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);

        log.info("Product Created....");
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("Get the product for productId: {}", productId);

        Product product
                = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException(
                        "Product with given Id not found",
                        "PRODUCT_NOT_FOUND"
                ));

        ProductResponse productResponse = new ProductResponse();
        copyProperties(product, productResponse);
        return productResponse;
    }

    @Override
    public void deleteProductById(long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductServiceCustomException(
                    "Product with given with id: " + productId + " not found:",
                    "PRODUCT_NOT_FOUND");
        }
        productRepository.deleteById(productId);

    }


}
