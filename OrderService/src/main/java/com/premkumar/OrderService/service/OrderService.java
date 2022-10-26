package com.premkumar.OrderService.service;

import com.premkumar.OrderService.model.OrderRequest;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
}
