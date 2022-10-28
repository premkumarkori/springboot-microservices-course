package com.premkumar.PaymentService.service;

import com.premkumar.PaymentService.model.PaymentRequest;
import com.premkumar.PaymentService.model.PaymentResponse;

public interface PaymentService {
    Object doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
