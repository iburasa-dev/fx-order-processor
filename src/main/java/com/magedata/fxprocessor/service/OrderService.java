package com.magedata.fxprocessor.service;

import com.magedata.fxprocessor.dto.CustomerSummaryResponse;
import com.magedata.fxprocessor.dto.OrderRequest;
import com.magedata.fxprocessor.dto.OrderResponse;
import com.magedata.fxprocessor.dto.request.OrderCreateRequest;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse getOrderById(UUID id);

    CustomerSummaryResponse getCustomerSummary(String customerId);

    default OrderResponse processOrder(OrderRequest request) {
        return createOrder(new OrderCreateRequest(
                request.customerId(),
                request.sourceCurrency(),
                request.targetCurrency(),
                request.items()
        ));
    }

    default OrderResponse createOrder(OrderRequest request) {
        return processOrder(request);
    }

    default OrderResponse getOrder(UUID id) {
        return getOrderById(id);
    }
}
