package com.premkumar.PaymentService.controller;

import com.premkumar.PaymentService.model.PaymentRequest;
import com.premkumar.PaymentService.model.PaymentResponse;
import com.premkumar.PaymentService.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/payments")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "This is to Make Payments from PlaceOrderAPI and Store PaymentDetails in DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Get OrderDetails from Db",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "BAD REQUEST",
                    content = @Content)
    })
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest) {

        long paymentId = (long) paymentService.doPayment(paymentRequest);
        log.info("payment Id: {}", paymentId);
        return new ResponseEntity<>(paymentId, HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "This is to Get OrderDetails from OrderService from Db")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Get OrderDetails from OrderService from Db",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
                    description = "NOT FOUND",
                    content = @Content)
    })
    public ResponseEntity<PaymentResponse> getOrderDetailsByOrderId(@PathVariable long orderId) {

        PaymentResponse response = paymentService.getPaymentDetailsByOrderId(orderId);
        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );

    }

}
