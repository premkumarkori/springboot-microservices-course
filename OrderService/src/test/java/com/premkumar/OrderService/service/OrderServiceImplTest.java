package com.premkumar.OrderService.service;

import com.premkumar.OrderService.entity.Order;
import com.premkumar.OrderService.model.OrderResponse;
import com.premkumar.OrderService.repository.OrderRepository;
import com.premkumar.clients.exception.CustomException;
import com.premkumar.clients.paymentservice.PaymentClient;
import com.premkumar.clients.paymentservice.PaymentMode;
import com.premkumar.clients.paymentservice.PaymentResponse;
import com.premkumar.clients.productservice.ProductClient;
import com.premkumar.clients.productservice.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceImplTest {


    @InjectMocks
    OrderService orderService = new OrderServiceImpl();
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductClient productClient;
    @Mock
    private PaymentClient paymentClient;

    @DisplayName("Getting Order Details --Success Scenario")
    @Test
    void getOrderDetails_When_Order_Success() {
        //Mocking
        Order order = getMockOrder();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(productClient.getProductById(anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(paymentClient.getOrderDetailsByOrderId(anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        //Actual
        OrderResponse orderResponse = orderService.getOrderDetails(1);

        //Verification
        verify(orderRepository, times(1)).findById(anyLong());
        verify(productClient, times(1)).getProductById(anyLong());
        verify(paymentClient, times(1)).getOrderDetailsByOrderId(anyLong());

        //Assert
        assertNotNull(orderResponse);
        assertEquals(order.getId(), orderResponse.getOrderId());
    }

    @DisplayName("Getting Order Details --Failure Scenario")
    @Test
    void getOrderDetails_NOTFOUND() {

        //Mocking
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        //Assert
        CustomException exception
                = assertThrows(CustomException.class, () -> orderService.getOrderDetails(1));
        assertEquals("NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getStatus());

        //Verify
        verify(orderRepository, times(1)).findById(anyLong());
    }

    private Order getMockOrder() {

        return Order.builder()
                .id(1)
                .quantity(200)
                .amount(1000)
                .orderDate(Instant.now())
                .orderStatus("PLACED")
                .productId(2)
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .productName("iPhone")
                .price(100)
                .quantity(200)
                .build();
    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .amount(200)
                .orderId(1)
                .status("ACCEPTED")
                .build();
    }

}