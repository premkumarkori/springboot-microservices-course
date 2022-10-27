package com.premkumar.PaymentService.controller;

import com.premkumar.PaymentService.model.PaymentRequest;
import com.premkumar.PaymentService.service.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/payments")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest) {

        long paymentId = (long) paymentService.doPayment(paymentRequest);
        log.info("Order Id: {}", paymentId);
        return new ResponseEntity<>(paymentId, HttpStatus.OK);
    }

}
