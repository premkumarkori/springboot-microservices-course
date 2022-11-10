package com.premkumar.PaymentService.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.premkumar.PaymentService.entity.TransactionDetails;
import com.premkumar.PaymentService.model.PaymentMode;
import com.premkumar.PaymentService.model.PaymentRequest;
import com.premkumar.PaymentService.model.PaymentResponse;
import com.premkumar.PaymentService.repository.TransactionDetailsRepository;
import com.premkumar.PaymentService.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest({"server.port=0"})
@AutoConfigureMockMvc
@EnableConfigurationProperties
class PaymentControllerTest {

    @RegisterExtension
    static WireMockExtension wireMockServer
            = WireMockExtension.newInstance()
            .options(
                    WireMockConfiguration
                            .wireMockConfig()
                            .port(8080)
            )
            .build();

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TransactionDetailsRepository transactionDetailsRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper
            = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void Test_1_When_doPayment_isSuccess() throws Exception {

        PaymentRequest paymentRequest = getMockPaymentRequest();

        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.post("/payments")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_internal")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String paymentId = mvcResult.getResponse().getContentAsString();

        Optional<TransactionDetails> transactionDetails = transactionDetailsRepository.findById(Long.valueOf(paymentId));
        assertTrue(transactionDetails.isPresent());

        TransactionDetails p = transactionDetails.get();

        assertEquals(Long.parseLong(paymentId), p.getId());
        assertEquals(paymentRequest.getPaymentMode().name(), p.getPaymentMode());
        assertEquals(paymentRequest.getAmount(), p.getAmount());
        assertEquals(paymentRequest.getOrderId(), p.getOrderId());
    }

    @Test
    void Test_2_When_doPayment_WithWrongAccess_thenThrow_403() throws Exception {

        PaymentRequest paymentRequest = getMockPaymentRequest();

        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.post("/payments")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();

    }

    @Test
    void Test_3_When_getOrderDetailsByOrderId_isSuccess() throws Exception {
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.get("/payments/orders/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_internal")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String actualResponse = mvcResult.getResponse().getContentAsString();
        TransactionDetails transactionDetails = transactionDetailsRepository.findById(1l).get();
        String expectedResponse = getMockPaymentResponse(transactionDetails);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void Test_4_When_getOrderDetailsByOrderId_isNotFound() throws Exception {
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.get("/payments/orders/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_internal")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }


    private PaymentRequest getMockPaymentRequest() {
        return PaymentRequest.builder()
                .amount(500)
                .orderId(1)
                .paymentMode(PaymentMode.CASH)
                .referenceNumber(null)
                .build();

    }

    private String getMockPaymentResponse(TransactionDetails transactionDetails) throws IOException {

        PaymentResponse paymentResponse
                = PaymentResponse.builder()
                .paymentId(transactionDetails.getId())
                .orderId(transactionDetails.getOrderId())
                .paymentDate(transactionDetails.getPaymentDate())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .status(transactionDetails.getPaymentStatus())
                .amount(transactionDetails.getAmount())
                .build();
        return objectMapper.writeValueAsString(paymentResponse);
    }
}