package com.magedata.fxprocessor.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.magedata.fxprocessor.dto.CustomerSummaryResponse;
import com.magedata.fxprocessor.dto.FeeBreakdownResponse;
import com.magedata.fxprocessor.dto.OrderItemRequest;
import com.magedata.fxprocessor.dto.OrderItemResponse;
import com.magedata.fxprocessor.dto.OrderResponse;
import com.magedata.fxprocessor.dto.request.OrderCreateRequest;
import com.magedata.fxprocessor.entity.OrderEntity;
import com.magedata.fxprocessor.entity.OrderItemEntity;
import com.magedata.fxprocessor.exception.ResourceNotFoundException;
import com.magedata.fxprocessor.repository.CustomerSummaryProjection;
import com.magedata.fxprocessor.repository.OrderRepository;
import com.magedata.fxprocessor.service.FeeCalculationService;
import com.magedata.fxprocessor.service.FeeResult;
import com.magedata.fxprocessor.service.FxRateResult;
import com.magedata.fxprocessor.service.FxRateService;
import com.magedata.fxprocessor.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final FxRateService fxRateService;
    private final FeeCalculationService feeCalculationService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            FxRateService fxRateService,
            FeeCalculationService feeCalculationService) {
        this.orderRepository = orderRepository;
        this.fxRateService = fxRateService;
        this.feeCalculationService = feeCalculationService;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        String sourceCurrency = request.getSourceCurrency().trim().toUpperCase();
        String targetCurrency = request.getTargetCurrency().trim().toUpperCase();

        BigDecimal sourceSubtotal = BigDecimal.ZERO;
        List<OrderItemEntity> itemEntities = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal lineTotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            sourceSubtotal = sourceSubtotal.add(lineTotal);

            OrderItemEntity itemEntity = OrderItemEntity.builder()
                    .description(itemReq.getDescription().trim())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice().setScale(4, RoundingMode.HALF_UP))
                    .lineTotal(lineTotal)
                    .build();

            itemEntities.add(itemEntity);
        }

        sourceSubtotal = sourceSubtotal.setScale(4, RoundingMode.HALF_UP);

        FxRateResult fxResult = fxRateService.getExchangeRate(sourceCurrency, targetCurrency);
        BigDecimal appliedRate = fxResult.rate();

        BigDecimal convertedSubtotal = sourceSubtotal.multiply(appliedRate).setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotalInUsd;
        if ("USD".equals(targetCurrency)) {
            subtotalInUsd = convertedSubtotal;
        } else if ("USD".equals(sourceCurrency)) {
            subtotalInUsd = sourceSubtotal.setScale(2, RoundingMode.HALF_UP);
        } else {
            FxRateResult usdFxResult = fxRateService.getExchangeRate(sourceCurrency, "USD");
            subtotalInUsd = sourceSubtotal.multiply(usdFxResult.rate()).setScale(2, RoundingMode.HALF_UP);
        }

        FeeResult feeResult = feeCalculationService.calculateFee(convertedSubtotal, subtotalInUsd);

        OrderEntity orderEntity = OrderEntity.builder()
                .id(UUID.randomUUID())
                .customerId(request.getCustomerId().trim())
                .sourceCurrency(sourceCurrency)
                .targetCurrency(targetCurrency)
                .sourceSubtotal(sourceSubtotal)
                .appliedExchangeRate(appliedRate)
                .rateSource(fxResult.rateSource())
                .convertedSubtotal(convertedSubtotal)
                .feePercentage(feeResult.feePercentage())
                .feeAmount(feeResult.feeAmount())
                .netTotal(feeResult.netTotal())
                .createdAt(OffsetDateTime.now())
                .build();

        for (OrderItemEntity item : itemEntities) {
            orderEntity.addItem(item);
        }

        OrderEntity savedOrder = orderRepository.save(orderEntity);
        log.info("Created order {} for customer {}", savedOrder.getId(), savedOrder.getCustomerId());

        return mapToOrderResponse(savedOrder, feeResult.tierDescription());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        OrderEntity order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        String tierDesc = order.getFeePercentage().compareTo(new BigDecimal("0.0100")) > 0
                ? "Tier 1: 1.5% fee for orders under $1,000 USD"
                : "Tier 2: 0.5% fee for orders $1,000 USD and above";

        return mapToOrderResponse(order, tierDesc);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSummaryResponse getCustomerSummary(String customerId) {
        CustomerSummaryProjection projection = orderRepository.findSummaryByCustomerId(customerId.trim());

        Long totalOrders = (projection != null && projection.getTotalOrders() != null)
                ? projection.getTotalOrders()
                : 0L;

        BigDecimal totalSpend = (projection != null && projection.getTotalSpend() != null)
                ? projection.getTotalSpend().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        BigDecimal cumulativeFees = (projection != null && projection.getCumulativeFeesPaid() != null)
                ? projection.getCumulativeFeesPaid().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        return CustomerSummaryResponse.builder()
                .customerId(customerId.trim())
                .totalOrders(totalOrders)
                .totalSpend(totalSpend)
                .cumulativeFeesPaid(cumulativeFees)
                .build();
    }

    private OrderResponse mapToOrderResponse(OrderEntity order, String tierDescription) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .description(item.getDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice().setScale(2, RoundingMode.HALF_UP))
                        .lineTotal(item.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        FeeBreakdownResponse feeBreakdown = FeeBreakdownResponse.builder()
                .tierDescription(tierDescription)
                .feePercentage(order.getFeePercentage())
                .feeAmount(order.getFeeAmount())
                .build();

        return OrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .sourceCurrency(order.getSourceCurrency())
                .targetCurrency(order.getTargetCurrency())
                .subtotal(order.getSourceSubtotal().setScale(2, RoundingMode.HALF_UP))
                .appliedConversionRate(order.getAppliedExchangeRate())
                .rateSource(order.getRateSource())
                .convertedTotal(order.getConvertedSubtotal())
                .feeBreakdown(feeBreakdown)
                .netTotal(order.getNetTotal())
                .creationTimestamp(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
