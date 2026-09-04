package com.magedata.fxprocessor.dto.request;

import com.magedata.fxprocessor.dto.OrderItemRequest;
import com.magedata.fxprocessor.dto.OrderRequest;

import java.util.List;

public class OrderCreateRequest {

    private String customerId;
    private String sourceCurrency;
    private String targetCurrency;
    private List<OrderItemRequest> items;

    public OrderCreateRequest() {
    }

    public OrderCreateRequest(String customerId, String sourceCurrency, String targetCurrency, List<OrderItemRequest> items) {
        this.customerId = customerId;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.items = items;
    }

    public OrderRequest toOrderRequest() {
        return new OrderRequest(customerId, sourceCurrency, targetCurrency, items);
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
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

        public OrderCreateRequest build() {
            return new OrderCreateRequest(customerId, sourceCurrency, targetCurrency, items);
        }
    }
}
