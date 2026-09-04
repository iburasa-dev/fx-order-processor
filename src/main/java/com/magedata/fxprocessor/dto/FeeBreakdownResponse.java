package com.magedata.fxprocessor.dto;

import java.math.BigDecimal;

public record FeeBreakdownResponse(
        String tierDescription,
        BigDecimal feePercentage,
        BigDecimal feeAmount
) {
    public String getTierDescription() {
        return tierDescription;
    }

    public BigDecimal getFeePercentage() {
        return feePercentage;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String tierDescription;
        private BigDecimal feePercentage;
        private BigDecimal feeAmount;

        public Builder tierDescription(String tierDescription) {
            this.tierDescription = tierDescription;
            return this;
        }

        public Builder feePercentage(BigDecimal feePercentage) {
            this.feePercentage = feePercentage;
            return this;
        }

        public Builder feeAmount(BigDecimal feeAmount) {
            this.feeAmount = feeAmount;
            return this;
        }

        public FeeBreakdownResponse build() {
            return new FeeBreakdownResponse(tierDescription, feePercentage, feeAmount);
        }
    }
}
