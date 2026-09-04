package com.magedata.fxprocessor.dto;

import java.math.BigDecimal;

public class CustomerSummaryResponse {

    private String customerId;
    private Long totalOrders;
    private BigDecimal totalSpend;
    private BigDecimal cumulativeFeesPaid;

    public CustomerSummaryResponse() {
    }

    public CustomerSummaryResponse(String customerId, Long totalOrders, BigDecimal totalSpend, BigDecimal cumulativeFeesPaid) {
        this.customerId = customerId;
        this.totalOrders = totalOrders;
        this.totalSpend = totalSpend;
        this.cumulativeFeesPaid = cumulativeFeesPaid;
    }

    public String customerId() {
        return customerId;
    }

    public Long totalOrders() {
        return totalOrders;
    }

    public BigDecimal totalSpend() {
        return totalSpend;
    }

    public BigDecimal cumulativeFeesPaid() {
        return cumulativeFeesPaid;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(BigDecimal totalSpend) {
        this.totalSpend = totalSpend;
    }

    public BigDecimal getCumulativeFeesPaid() {
        return cumulativeFeesPaid;
    }

    public void setCumulativeFeesPaid(BigDecimal cumulativeFeesPaid) {
        this.cumulativeFeesPaid = cumulativeFeesPaid;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String customerId;
        private Long totalOrders;
        private BigDecimal totalSpend;
        private BigDecimal cumulativeFeesPaid;

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }

        public Builder totalSpend(BigDecimal totalSpend) {
            this.totalSpend = totalSpend;
            return this;
        }

        public Builder cumulativeFeesPaid(BigDecimal cumulativeFeesPaid) {
            this.cumulativeFeesPaid = cumulativeFeesPaid;
            return this;
        }

        public CustomerSummaryResponse build() {
            return new CustomerSummaryResponse(customerId, totalOrders, totalSpend, cumulativeFeesPaid);
        }
    }
}
