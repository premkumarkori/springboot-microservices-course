package com.premkumar.clients.paymentservice;

import com.premkumar.clients.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "paymentservice/payments", configuration = FeignConfig.class)
public interface PaymentClient {

    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    @GetMapping("/order/{orderId}")
    ResponseEntity<PaymentResponse> getOrderDetailsByOrderId(@PathVariable("orderId") String orderId);
}
