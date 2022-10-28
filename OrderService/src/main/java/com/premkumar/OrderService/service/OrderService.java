package com.premkumar.OrderService.service;

import com.premkumar.OrderService.model.OrderRequest;
import com.premkumar.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
