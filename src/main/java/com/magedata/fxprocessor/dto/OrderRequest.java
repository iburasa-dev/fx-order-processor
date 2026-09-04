package com.magedata.fxprocessor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record OrderRequest(
        @NotBlank(message = "customerId is required")
        String customerId,

        @NotBlank(message = "sourceCurrency is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "sourceCurrency must be a 3-letter ISO code")
        String sourceCurrency,

        @NotBlank(message = "targetCurrency is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "targetCurrency must be a 3-letter ISO code")
        String targetCurrency,

        @NotEmpty(message = "items list cannot be empty")
        @Valid
        List<OrderItemRequest> items
) {
    public String getCustomerId() {
        return customerId;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String customerId;
        private String sourceCurrency;
        private String targetCurrency;
        private List<OrderItemRequest> items;

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

        public Builder items(List<OrderItemRequest> items) {
            this.items = items;
            return this;
        }

        public OrderRequest build() {
            return new OrderRequest(customerId, sourceCurrency, targetCurrency, items);
        }
    }
}
