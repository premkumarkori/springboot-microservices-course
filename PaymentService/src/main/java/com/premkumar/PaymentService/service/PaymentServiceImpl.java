package com.premkumar.PaymentService.service;

import com.premkumar.PaymentService.entity.TransactionDetails;
import com.premkumar.PaymentService.exception.PaymentServiceCustomException;
import com.premkumar.PaymentService.model.PaymentMode;
import com.premkumar.PaymentService.model.PaymentRequest;
import com.premkumar.PaymentService.model.PaymentResponse;
import com.premkumar.PaymentService.repository.TransactionDetailsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransactionDetailsRepository transactionDetailsRepository;

    @Override
    public Object doPayment(PaymentRequest paymentRequest) {

        log.info("Recording Payment Details: {}", paymentRequest);
        TransactionDetails transactionDetails
                = TransactionDetails.builder()
                .orderId(paymentRequest.getOrderId())
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCESS")
                .referenceNumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();

        transactionDetailsRepository.save(transactionDetails);

        log.info("Transaction Completed with Id: {}", transactionDetails.getId());

        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(long orderId) {
        log.info("Getting payment details for the Order Id: {}", orderId);

        TransactionDetails transactionDetails
                = transactionDetailsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentServiceCustomException(
                        "TransactionDetails with given id not found",
                        "TRANSACTION_NOT_FOUND"));

        PaymentResponse paymentResponse
                = PaymentResponse.builder()
                .paymentId(transactionDetails.getId())
                .amount(transactionDetails.getAmount())
                .orderId(transactionDetails.getOrderId())
                .paymentDate(transactionDetails.getPaymentDate())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .status(transactionDetails.getPaymentStatus())
                .build();
        return paymentResponse;
    }
}
