package com.premkumar.PaymentService.service;

import com.premkumar.PaymentService.model.PaymentRequest;

public interface PaymentService {
    Object doPayment(PaymentRequest paymentRequest);
}
