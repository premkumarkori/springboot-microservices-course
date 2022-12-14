package com.premkumar.ProductService.service;

import com.premkumar.ProductService.entity.Product;
import com.premkumar.ProductService.exception.ProductServiceCustomException;
import com.premkumar.ProductService.model.ProductRequest;
import com.premkumar.ProductService.model.ProductResponse;
import com.premkumar.ProductService.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductServiceImplTest {


    @InjectMocks
    private ProductService productService = new ProductServiceImpl();
    @Mock
    private ProductRepository productRepository;

    @Test
    void test_When_addProduct_isSuccess() {

        ProductRequest productRequest = getMockProductRequest();
        Product product = getMockProductDetails();

        when(productRepository.save(Mockito.any(Product.class))).thenReturn(product);

        long productId = productService.addProduct(productRequest);

        verify(productRepository, times(1))
                .save(any());

        assertEquals(product.getProductId(), productId);
    }


    @Test
    void test_When_GetProductById_isSuccess() {
        Product product = getMockProductDetails();
        when(productRepository.findById(product.getProductId())).thenReturn(Optional.of(product));

        ProductResponse productResponse = productService.getProductById(1);
        //Verification
        verify(productRepository, times(1)).findById(anyLong());

        //Assert
        assertNotNull(productResponse);
        assertEquals(product.getProductId(), productResponse.getProductId());

    }

    @Test
    void test_When_GetProductById_isNotFound() {

        when(productRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
        //Assert
        ProductServiceCustomException exception
                = assertThrows(ProductServiceCustomException.class, () -> productService.getProductById(1));
        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        assertEquals("Product with given Id not found", exception.getMessage());

        //Verify
        verify(productRepository, times(1)).findById(anyLong());


    }

    @Test
    void test_When_deleteProductById_isNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
        //Assert
        ProductServiceCustomException exception
                = assertThrows(ProductServiceCustomException.class, () -> productService.deleteProductById(1));
        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        assertEquals("Product with given with id: 1 not found:", exception.getMessage());

        //Verify
        verify(productRepository, times(0)).deleteById(anyLong());

    }

    @Test
    void test_When_deleteProductById_isSuccess() {

        Product product = getMockProductDetails();
        when(productRepository.existsById(product.getProductId())).thenReturn(true);

        productService.deleteProductById(1);
        //Verification
        verify(productRepository, times(1)).deleteById(anyLong());

    }

    @Test
    void test_When_reduceQuantity_isSuccess() {
        Product product = getMockProductDetails();
        long productId = 1, quantity = 5;
        when(productRepository.findById(product.getProductId())).thenReturn(Optional.of(product));
        productService.reduceQuantity(productId, quantity);
        when(productRepository.save(Mockito.any(Product.class))).thenReturn(product);
        verify(productRepository, times(1))
                .save(any());
    }

    @Test
    void test_When_reduceQuantity_isFailed_when_productId_isNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        //Assert
        ProductServiceCustomException exception
                = assertThrows(ProductServiceCustomException.class, () -> productService.reduceQuantity(1, 1));
        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        assertEquals("Product with given Id not found", exception.getMessage());

        //Verify
        verify(productRepository, times(0)).save(any());
    }

    @Test
    void test_When_reduceQuantity_isFailed_when_insufficientQuantity() {
        Product product = getMockProductDetails();
        when(productRepository.findById(product.getProductId())).thenReturn(Optional.of(product));
        ProductServiceCustomException exception
                = assertThrows(ProductServiceCustomException.class, () -> productService.reduceQuantity(1, 11));
        assertEquals("INSUFFICIENT_QUANTITY", exception.getErrorCode());
        assertEquals("Product does not have sufficient Quantity", exception.getMessage());

        //Verify
        verify(productRepository, times(0)).save(any());
    }

    private Product getMockProductDetails() {

        return Product.builder()
                .productName("iphone")
                .quantity(10)
                .productId(1)
                .price(1000)
                .build();

    }

    private ProductRequest getMockProductRequest() {
        return ProductRequest.builder()
                .name("iphone")
                .price(1000)
                .quantity(10)
                .build();
    }

}