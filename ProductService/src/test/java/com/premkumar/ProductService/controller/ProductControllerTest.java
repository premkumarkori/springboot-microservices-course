package com.premkumar.ProductService.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.premkumar.ProductService.entity.Product;
import com.premkumar.ProductService.model.ProductRequest;
import com.premkumar.ProductService.model.ProductResponse;
import com.premkumar.ProductService.repository.ProductRepository;
import com.premkumar.ProductService.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
@Slf4j
@TestMethodOrder(MethodOrderer.MethodName.class)
class ProductControllerTest {

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
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper
            = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void Test_1_WhenaddProduct_isSuccess() throws Exception {
        ProductRequest productRequest = getMockProductRequest();

        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String productId = mvcResult.getResponse().getContentAsString();

        Optional<Product> product = productRepository.findById(Long.valueOf(productId));
        assertTrue(product.isPresent());

        Product p = product.get();

        assertEquals(Long.parseLong(productId), p.getProductId());
        assertEquals(productRequest.getQuantity(), p.getQuantity());
        assertEquals(productRequest.getName(), p.getProductName());
        assertEquals(productRequest.getPrice(), p.getPrice());

    }

    @Test
    void Test_2_When_addProduct_WithWrongAccess_thenThrow_403() throws Exception {
        ProductRequest productRequest = getMockProductRequest();

        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Customer")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    void Test_3_When_getProductById_isSuccess() throws Exception {
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.get("/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String actualResponse = mvcResult.getResponse().getContentAsString();
        Product product = productRepository.findById(1l).get();
        String expectedResponse = getMockProductResponse(product);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void Test_4_When_getProductById_isNotFound() throws Exception {
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.get("/products/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    @Test
    void Test_5_When_reduceQuantity_isSuccess() throws Exception {


        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.put("/products/reduceQuantity/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .param("quantity", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

    }

    @Test
    void Test_6_When_reduceQuantity_isFailed_for_productId_isNotFound() throws Exception {


        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.put("/products/reduceQuantity/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .param("quantity", String.valueOf(1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

    }

    @Test
    void Test_7_When_reduceQuantity_isFailed_for_inSufficient_quantity() throws Exception {


        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.put("/products/reduceQuantity/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .param("quantity", String.valueOf(11))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

    }

    @Test
    void Test_8_When_deleteProductById_isSuccess() throws Exception {

        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.delete("/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    void Test_9_when_deleteProductById_when_productId_isNotFound() throws Exception {

        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.delete("/products/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    @Test
    void Test_10_when_deleteProductById_WithWrongAccess_thenThrow_403() throws Exception {

        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.delete("/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Customer")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }


    private ProductRequest getMockProductRequest() {
        return ProductRequest.builder()
                .name("iphone")
                .price(1000)
                .quantity(10)
                .build();
    }


    private String getMockProductResponse(Product product) throws IOException {

        ProductResponse productResponse
                = ProductResponse.builder()
                .productName(product.getProductName())
                .quantity(product.getQuantity())
                .productId(product.getProductId())
                .price(product.getPrice())
                .build();
        return objectMapper.writeValueAsString(productResponse);
    }

}