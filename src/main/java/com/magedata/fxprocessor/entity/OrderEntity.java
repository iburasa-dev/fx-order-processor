package com.magedata.fxprocessor.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
})
public class OrderEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "source_currency", nullable = false, length = 3)
    private String sourceCurrency;

    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(name = "source_subtotal", nullable = false, precision = 18, scale = 4)
    private BigDecimal sourceSubtotal;

    @Column(name = "applied_exchange_rate", nullable = false, precision = 18, scale = 6)
    private BigDecimal appliedExchangeRate;

    @Column(name = "rate_source", nullable = false, length = 32)
    private String rateSource;

    @Column(name = "converted_subtotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal convertedSubtotal;

    @Column(name = "fee_percentage", nullable = false, precision = 6, scale = 4)
    private BigDecimal feePercentage;

    @Column(name = "fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "net_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal netTotal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemEntity> items = new ArrayList<>();

    public OrderEntity() {
    }

    public OrderEntity(UUID id, String customerId, String sourceCurrency, String targetCurrency,
                       BigDecimal sourceSubtotal, BigDecimal appliedExchangeRate, String rateSource,
                       BigDecimal convertedSubtotal, BigDecimal feePercentage, BigDecimal feeAmount,
                       BigDecimal netTotal, OffsetDateTime createdAt, List<OrderItemEntity> items) {
        this.id = id;
        this.customerId = customerId;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.sourceSubtotal = sourceSubtotal;
        this.appliedExchangeRate = appliedExchangeRate;
        this.rateSource = rateSource;
        this.convertedSubtotal = convertedSubtotal;
        this.feePercentage = feePercentage;
        this.feeAmount = feeAmount;
        this.netTotal = netTotal;
        this.createdAt = createdAt;
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public BigDecimal getSourceSubtotal() {
        return sourceSubtotal;
    }

    public void setSourceSubtotal(BigDecimal sourceSubtotal) {
        this.sourceSubtotal = sourceSubtotal;
    }

    public BigDecimal getAppliedExchangeRate() {
        return appliedExchangeRate;
    }

    public void setAppliedExchangeRate(BigDecimal appliedExchangeRate) {
        this.appliedExchangeRate = appliedExchangeRate;
    }

    public String getRateSource() {
        return rateSource;
    }

    public void setRateSource(String rateSource) {
        this.rateSource = rateSource;
    }

    public BigDecimal getConvertedSubtotal() {
        return convertedSubtotal;
    }

    public void setConvertedSubtotal(BigDecimal convertedSubtotal) {
        this.convertedSubtotal = convertedSubtotal;
    }

    public BigDecimal getFeePercentage() {
        return feePercentage;
    }

    public void setFeePercentage(BigDecimal feePercentage) {
        this.feePercentage = feePercentage;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(BigDecimal netTotal) {
        this.netTotal = netTotal;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEntity> items) {
        this.items = items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String customerId;
        private String sourceCurrency;
        private String targetCurrency;
        private BigDecimal sourceSubtotal;
        private BigDecimal appliedExchangeRate;
        private String rateSource;
        private BigDecimal convertedSubtotal;
        private BigDecimal feePercentage;
        private BigDecimal feeAmount;
        private BigDecimal netTotal;
        private OffsetDateTime createdAt;
        private List<OrderItemEntity> items = new ArrayList<>();

        public Builder id(UUID id) {
            this.id = id;
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

        public Builder sourceSubtotal(BigDecimal sourceSubtotal) {
            this.sourceSubtotal = sourceSubtotal;
            return this;
        }

        public Builder appliedExchangeRate(BigDecimal appliedExchangeRate) {
            this.appliedExchangeRate = appliedExchangeRate;
            return this;
        }

        public Builder rateSource(String rateSource) {
            this.rateSource = rateSource;
            return this;
        }

        public Builder convertedSubtotal(BigDecimal convertedSubtotal) {
            this.convertedSubtotal = convertedSubtotal;
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

        public Builder netTotal(BigDecimal netTotal) {
            this.netTotal = netTotal;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder items(List<OrderItemEntity> items) {
            this.items = items != null ? items : new ArrayList<>();
            return this;
        }

        public OrderEntity build() {
            return new OrderEntity(id, customerId, sourceCurrency, targetCurrency, sourceSubtotal,
                    appliedExchangeRate, rateSource, convertedSubtotal, feePercentage, feeAmount,
                    netTotal, createdAt, items);
        }
    }
}
