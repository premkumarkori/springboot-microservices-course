package com.premkumar.OrderService.controller;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.premkumar.OrderService.OrderServiceConfig;
import com.premkumar.OrderService.entity.Order;
import com.premkumar.OrderService.model.OrderRequest;
import com.premkumar.OrderService.model.OrderResponse;
import com.premkumar.OrderService.repository.OrderRepository;
import com.premkumar.OrderService.service.OrderService;
import com.premkumar.clients.paymentservice.PaymentMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.Charset.defaultCharset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.util.StreamUtils.copyToString;


@SpringBootTest({"server.port=0"})
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ContextConfiguration(classes = {OrderServiceConfig.class})

public class OrderControllerTest {

    @RegisterExtension
    static WireMockExtension wireMockserver
            = WireMockExtension.newInstance()
            .options(WireMockConfiguration
                    .wireMockConfig()
                    .port(8080))
            .build();

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper
            = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @BeforeEach
    void setup() throws IOException {
        getProductDetailsResponse();
        doPayment();
        getPaymentDetails();
        reduceQuantity();
    }

    private void reduceQuantity() {
        wireMockserver.stubFor(put(urlMatching("/products/reduceQuantity/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getPaymentDetails() throws IOException {
        wireMockserver.stubFor(get("/payments/orders/1")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBody(copyToString(OrderControllerTest.class.getClassLoader().getResourceAsStream("mock/GetPayment.json"), defaultCharset()))));
    }

    private void doPayment() {
        wireMockserver.stubFor(post(urlEqualTo("/payments"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getProductDetailsResponse() throws IOException {

        wireMockserver.stubFor(get("/products/1")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(copyToString(
                                OrderControllerTest.class
                                        .getClassLoader()
                                        .getResourceAsStream("mock/GetProduct.json"),
                                defaultCharset()))));
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .paymentMode(PaymentMode.CASH)
                .quantity(10)
                .totalAmount(200)
                .build();
    }

    @Test
    @DisplayName("Place Order -- Success Scenario")
    void Test_1_When_placeOrder_DoPayment_Success() throws Exception {
        //Place Order
        //Get Order By Order Id from Db and check
        //Check output

        OrderRequest orderRequest = getMockOrderRequest();
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.post("/orders/placeOrder")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Customer")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String orderId = mvcResult.getResponse().getContentAsString();

        Optional<Order> order = orderRepository.findById(Long.valueOf(orderId));
        assertTrue(order.isPresent());

        Order o = order.get();
        assertEquals(Long.parseLong(orderId), o.getId());
        assertEquals("PLACED", o.getOrderStatus());
        assertEquals(orderRequest.getTotalAmount(), o.getAmount());
        assertEquals(orderRequest.getQuantity(), o.getQuantity());

    }

    @Test
    @DisplayName("Place Order -- Failure Scenario")
    public void Test_2_When_placeOrder_WithWrongAccess_thenThrow_403() throws Exception {


        OrderRequest orderRequest = getMockOrderRequest();
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.post("/orders/placeOrder")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();

    }

    @Test
    @DisplayName("Get Order -- Success Scenario")
    public void Test_3_When_getOrderDetails_isSuccess() throws Exception {
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.get("/orders/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String actualResponse = mvcResult.getResponse().getContentAsString();
        Order order = orderRepository.findById(1l).get();
        String expectedResponse = getOrderResponse(order);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Get Order -- Failure Scenario")
    public void Test_4_When_getOrderDetails_Not_Found() throws Exception {
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.get("/orders/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

    }

    private String getOrderResponse(Order order) throws IOException {
        OrderResponse.PaymentDetails paymentDetails
                = objectMapper.readValue(
                copyToString(
                        OrderControllerTest.class.getClassLoader()
                                .getResourceAsStream("mock/GetPayment.json"
                                ),
                        defaultCharset()
                ), OrderResponse.PaymentDetails.class
        );
        paymentDetails.setPaymentStatus("SUCCESS");

        OrderResponse.ProductDetails productDetails
                = objectMapper.readValue(
                copyToString(
                        OrderControllerTest.class.getClassLoader()
                                .getResourceAsStream("mock/GetProduct.json"),
                        defaultCharset()
                ), OrderResponse.ProductDetails.class
        );

        OrderResponse orderResponse
                = OrderResponse.builder()
                .paymentDetails(paymentDetails)
                .productDetails(productDetails)
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .orderId(order.getId())
                .build();
        return objectMapper.writeValueAsString(orderResponse);
    }

}