package com.premkumar.ProductService.exception;

import lombok.Data;

@Data
public class ProductServiceCustomException extends RuntimeException {
    private final String errorCode;

    public ProductServiceCustomException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
