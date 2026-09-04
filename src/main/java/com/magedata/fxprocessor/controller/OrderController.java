package com.magedata.fxprocessor.controller;

import com.magedata.fxprocessor.dto.CustomerSummaryResponse;
import com.magedata.fxprocessor.dto.OrderRequest;
import com.magedata.fxprocessor.dto.OrderResponse;
import com.magedata.fxprocessor.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> submitOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.processOrder(request);
        return ResponseEntity
                .created(URI.create("/api/v1/orders/" + response.orderId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/summary")
    public CustomerSummaryResponse getCustomerSummary(
            @RequestParam @NotBlank(message = "customerId is required") String customerId) {
        return orderService.getCustomerSummary(customerId);
    }
}
