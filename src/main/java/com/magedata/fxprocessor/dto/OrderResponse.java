package com.magedata.fxprocessor.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String customerId,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal subtotal,
        BigDecimal appliedConversionRate,
        String rateSource,
        BigDecimal convertedTotal,
        FeeBreakdownResponse feeBreakdown,
        BigDecimal netTotal,
        OffsetDateTime creationTimestamp,
        List<OrderItemResponse> items
) {
    public UUID getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getAppliedConversionRate() {
        return appliedConversionRate;
    }

    public String getRateSource() {
        return rateSource;
    }

    public BigDecimal getConvertedTotal() {
        return convertedTotal;
    }

    public FeeBreakdownResponse getFeeBreakdown() {
        return feeBreakdown;
    }

    public BigDecimal getNetTotal() {
        return netTotal;
    }

    public OffsetDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID orderId;
        private String customerId;
        private String sourceCurrency;
        private String targetCurrency;
        private BigDecimal subtotal;
        private BigDecimal appliedConversionRate;
        private String rateSource;
        private BigDecimal convertedTotal;
        private FeeBreakdownResponse feeBreakdown;
        private BigDecimal netTotal;
        private OffsetDateTime creationTimestamp;
        private List<OrderItemResponse> items;

        public Builder orderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder sourceCurrency(String sourceCurrency) {
            this.sourceCurrency = sourceCurrency;
            return this;
        }

        public Builder targetCurrency(String targetCurrency) {
            this.targetCurrency = targetCurrency;
            return this;
        }

        public Builder subtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public Builder appliedConversionRate(BigDecimal appliedConversionRate) {
            this.appliedConversionRate = appliedConversionRate;
            return this;
        }

        public Builder rateSource(String rateSource) {
            this.rateSource = rateSource;
            return this;
        }

        public Builder convertedTotal(BigDecimal convertedTotal) {
            this.convertedTotal = convertedTotal;
            return this;
        }

        public Builder feeBreakdown(FeeBreakdownResponse feeBreakdown) {
            this.feeBreakdown = feeBreakdown;
            return this;
        }

        public Builder netTotal(BigDecimal netTotal) {
            this.netTotal = netTotal;
            return this;
        }

        public Builder creationTimestamp(OffsetDateTime creationTimestamp) {
            this.creationTimestamp = creationTimestamp;
            return this;
        }

        public Builder items(List<OrderItemResponse> items) {
            this.items = items;
            return this;
        }

        public OrderResponse build() {
            return new OrderResponse(orderId, customerId, sourceCurrency, targetCurrency, subtotal,
                    appliedConversionRate, rateSource, convertedTotal, feeBreakdown, netTotal,
                    creationTimestamp, items);
        }
    }
}
