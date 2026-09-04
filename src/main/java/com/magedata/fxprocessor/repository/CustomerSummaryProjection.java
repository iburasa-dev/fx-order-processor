package com.magedata.fxprocessor.repository;

import java.math.BigDecimal;

public interface CustomerSummaryProjection {
    Long getTotalOrders();
    BigDecimal getTotalSpend();
    BigDecimal getCumulativeFeesPaid();
}
