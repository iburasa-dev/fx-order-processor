package com.magedata.fxprocessor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magedata.fxprocessor.dto.FeeBreakdownResponse;
import com.magedata.fxprocessor.dto.OrderItemRequest;
import com.magedata.fxprocessor.dto.OrderItemResponse;
import com.magedata.fxprocessor.dto.OrderRequest;
import com.magedata.fxprocessor.dto.OrderResponse;
import com.magedata.fxprocessor.dto.CustomerSummaryResponse;
import com.magedata.fxprocessor.exception.ResourceNotFoundException;
import com.magedata.fxprocessor.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/v1/orders creates order successfully and returns 201 with Location header")
    void testCreateOrder_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(
                "cust-101",
                "EUR",
                "USD",
                List.of(new OrderItemRequest("Wireless Mouse", 1, new BigDecimal("50.00")))
        );

        OrderResponse mockResponse = new OrderResponse(
                orderId,
                "cust-101",
                "EUR",
                "USD",
                new BigDecimal("50.00"),
                new BigDecimal("1.085000"),
                "LIVE_API",
                new BigDecimal("54.25"),
                new FeeBreakdownResponse("Tier 1 (1.5%)", new BigDecimal("0.0150"), new BigDecimal("0.81")),
                new BigDecimal("55.06"),
                OffsetDateTime.now(),
                List.of(new OrderItemResponse(1L, "Wireless Mouse", 1, new BigDecimal("50.00"), new BigDecimal("50.00")))
        );

        when(orderService.processOrder(any(OrderRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/orders/" + orderId))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value("cust-101"))
                .andExpect(jsonPath("$.subtotal").value(50.00))
                .andExpect(jsonPath("$.appliedConversionRate").value(1.085))
                .andExpect(jsonPath("$.rateSource").value("LIVE_API"))
                .andExpect(jsonPath("$.netTotal").value(55.06));
    }

    @Test
    @DisplayName("POST /api/v1/orders with empty items list returns 400 Bad Request")
    void testCreateOrder_ValidationFailure() throws Exception {
        OrderRequest invalidRequest = new OrderRequest(
                "",
                "EUR",
                "USD",
                List.of()
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} returns 200 and order details")
    void testGetOrderById_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse mockResponse = new OrderResponse(
                orderId,
                "cust-101",
                "EUR",
                "USD",
                new BigDecimal("100.00"),
                new BigDecimal("1.085000"),
                "LIVE_API",
                new BigDecimal("108.50"),
                new FeeBreakdownResponse("Tier 1", new BigDecimal("0.0150"), new BigDecimal("1.63")),
                new BigDecimal("110.13"),
                OffsetDateTime.now(),
                List.of()
        );

        when(orderService.getOrder(orderId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value("cust-101"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} returns 404 when order does not exist")
    void testGetOrderById_NotFound() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrder(orderId))
                .thenThrow(new ResourceNotFoundException("Order not found with ID: " + orderId));

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/orders/summary returns 200 and customer aggregate metrics")
    void testGetCustomerSummary_Success() throws Exception {
        CustomerSummaryResponse mockSummary = new CustomerSummaryResponse(
                "cust-101",
                3L,
                new BigDecimal("750.50"),
                new BigDecimal("11.25")
        );

        when(orderService.getCustomerSummary("cust-101")).thenReturn(mockSummary);

        mockMvc.perform(get("/api/v1/orders/summary").param("customerId", "cust-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("cust-101"))
                .andExpect(jsonPath("$.totalOrders").value(3))
                .andExpect(jsonPath("$.totalSpend").value(750.50))
                .andExpect(jsonPath("$.cumulativeFeesPaid").value(11.25));
    }
}
