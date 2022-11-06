package com.premkumar.PaymentService.service;

import com.premkumar.PaymentService.model.PaymentRequest;
import com.premkumar.PaymentService.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(long orderId);
}
