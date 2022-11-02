package com.premkumar.OrderService.service;

import com.premkumar.OrderService.entity.Order;
import com.premkumar.OrderService.model.OrderRequest;
import com.premkumar.OrderService.model.OrderResponse;
import com.premkumar.OrderService.repository.OrderRepository;
import com.premkumar.clients.exception.CustomException;
import com.premkumar.clients.paymentservice.PaymentClient;
import com.premkumar.clients.paymentservice.PaymentRequest;
import com.premkumar.clients.paymentservice.PaymentResponse;
import com.premkumar.clients.productservice.ProductClient;
import com.premkumar.clients.productservice.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j

public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private PaymentClient paymentClient;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        //Order Entity -> Save the data with Status Order Created
        //Product Service - Block Products (Reduce the Quantity)
        //Payment Service -> Payments -> Success-> COMPLETE, Else
        //CANCELLED
        log.info("Placing Order Request: {}", orderRequest);

        productClient.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                .orderDate(Instant.now())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .amount(orderRequest.getTotalAmount())
                .build();
        orderRepository.save(order);

        log.info("Calling Payment Service to complete the payment");

        PaymentRequest paymentRequest
                = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus;


        try {
            paymentClient.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the Oder status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);

        orderRepository.save(order);

        log.info("Order Places successfully with Order Id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for Order Id : {}", orderId);

        Order order
                = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for the order Id:" + orderId,
                        "NOT_FOUND",
                        404));

        log.info("Invoking Product service to fetch the product for id: {}", order.getProductId());

        ResponseEntity<ProductResponse> productResponse = productClient.getProductById(order.getProductId());
        ProductResponse productResponseBody = productResponse.getBody();

        log.info("Getting payment information form the payment Service");
        ResponseEntity<PaymentResponse> paymentResponse = paymentClient.getOrderDetailsByOrderId(order.getId());
        PaymentResponse paymentResponseBody = paymentResponse.getBody();

        OrderResponse.PaymentDetails paymentDetails = new OrderResponse.PaymentDetails();
        OrderResponse.ProductDetails productDetails = new OrderResponse.ProductDetails();

        if (paymentResponseBody != null && productResponseBody != null) {
            paymentDetails = OrderResponse.PaymentDetails.builder()
                    .paymentId(paymentResponseBody.getPaymentId())
                    .paymentDate(paymentResponseBody.getPaymentDate())
                    .paymentMode(paymentResponseBody.getPaymentMode())
                    .paymentStatus(paymentResponseBody.getStatus())
                    .build();

            productDetails = OrderResponse.ProductDetails.builder()
                    .price(productResponseBody.getPrice())
                    .productName(productResponseBody.getProductName())
                    .productId(productResponseBody.getProductId())
                    .quantity(productResponseBody.getQuantity())
                    .build();
        }

        OrderResponse orderResponse
                = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        return orderResponse;
    }
}
