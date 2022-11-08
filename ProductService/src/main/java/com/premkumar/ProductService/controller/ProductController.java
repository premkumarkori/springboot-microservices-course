package com.premkumar.ProductService.controller;

import com.premkumar.ProductService.model.ProductRequest;
import com.premkumar.ProductService.model.ProductResponse;
import com.premkumar.ProductService.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@Slf4j
@AllArgsConstructor
public class ProductController {

    private ProductService productService;

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping
    @Operation(summary = "This is to Post Products in Db By Admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Stored All the Products into Db",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = @Content)
    })
    public ResponseEntity<Long> addProduct(@RequestBody ProductRequest productRequest) {
        log.info("adding a Product into DB {}", productRequest);
        long productId = productService.addProduct(productRequest);
        return new ResponseEntity<>(productId, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer') || hasAuthority('SCOPE_internal')")
    @GetMapping("/{id}")
    @Operation(summary = "This is to Get Products in Db By Admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Get All the Products from Db",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500",
                    description = "Product Not Available",
                    content = @Content)
    })
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") long productId) {
        ProductResponse productResponse
                = productService.getProductById(productId);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{id}")
    @Operation(summary = "This is to Delete Products in Db By Admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Delete the Products By Id from Db",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500",
                    description = "Product Not Available",
                    content = @Content)
    })
    public void deleteProductById(@PathVariable("id") long productId) {
        productService.deleteProductById(productId);
    }

    @PutMapping("/reduceQuantity/{id}")
    @Operation(summary = "This is to Update Products in Db whenever an Order is Placed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Update the Products By Quantity in Db",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
                    description = "Product Not Available",
                    content = @Content)
    })
    public ResponseEntity<Void> reduceQuantity(@PathVariable("id") long productId,
                                               @RequestParam long quantity) {
        productService.reduceQuantity(productId, quantity);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
