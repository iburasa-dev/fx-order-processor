package com.magedata.fxprocessor.dto;

import java.math.BigDecimal;

public class OrderSummaryResponse extends CustomerSummaryResponse {

    public OrderSummaryResponse() {
        super();
    }

    public OrderSummaryResponse(String customerId, Long totalOrders, BigDecimal totalSpend, BigDecimal cumulativeFeesPaid) {
        super(customerId, totalOrders, totalSpend, cumulativeFeesPaid);
    }
}
