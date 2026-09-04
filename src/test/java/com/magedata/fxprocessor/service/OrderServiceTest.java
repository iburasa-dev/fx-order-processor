package com.magedata.fxprocessor.service;

import com.magedata.fxprocessor.dto.OrderItemRequest;
import com.magedata.fxprocessor.dto.OrderRequest;
import com.magedata.fxprocessor.dto.OrderResponse;
import com.magedata.fxprocessor.dto.CustomerSummaryResponse;
import com.magedata.fxprocessor.entity.OrderEntity;
import com.magedata.fxprocessor.entity.OrderItemEntity;
import com.magedata.fxprocessor.exception.ResourceNotFoundException;
import com.magedata.fxprocessor.repository.CustomerSummaryProjection;
import com.magedata.fxprocessor.repository.OrderRepository;
import com.magedata.fxprocessor.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FxRateService fxRateService;

    @Mock
    private FeeCalculationService feeCalculationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest sampleRequest;

    @BeforeEach
    void setUp() {
        OrderItemRequest item1 = new OrderItemRequest("Keychron K2 Keyboard", 2, new BigDecimal("100.00"));
        OrderItemRequest item2 = new OrderItemRequest("Logitech MX Master 3S", 1, new BigDecimal("150.00"));

        sampleRequest = new OrderRequest("cust-42", "EUR", "USD", List.of(item1, item2));
    }

    @Test
    @DisplayName("Successfully creates order, computes financial audit trail, and persists")
    void testCreateOrder_Success() {
        when(fxRateService.getExchangeRate("EUR", "USD"))
                .thenReturn(new FxRateResult(new BigDecimal("1.100000"), FxRateResult.SOURCE_LIVE_API));

        FeeResult mockFeeResult = new FeeResult(
                new BigDecimal("0.0150"),
                new BigDecimal("5.78"),
                new BigDecimal("390.78"),
                "Standard Tier: 1.5% fee applied"
        );
        when(feeCalculationService.calculateFee(any(), any())).thenReturn(mockFeeResult);

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(OffsetDateTime.now());
            return saved;
        });

        OrderResponse response = orderService.processOrder(sampleRequest);

        assertNotNull(response);
        assertNotNull(response.orderId());
        assertEquals("cust-42", response.customerId());
        assertEquals("EUR", response.sourceCurrency());
        assertEquals("USD", response.targetCurrency());
        assertEquals(new BigDecimal("350.00"), response.subtotal());
        assertEquals(new BigDecimal("1.100000"), response.appliedConversionRate());
        assertEquals(FxRateResult.SOURCE_LIVE_API, response.rateSource());
        assertEquals(new BigDecimal("385.00"), response.convertedTotal());
        assertEquals(new BigDecimal("5.78"), response.feeBreakdown().feeAmount());
        assertEquals(new BigDecimal("390.78"), response.netTotal());
        assertEquals(2, response.items().size());

        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Retrieves order by ID with line items")
    void testGetOrderById_Success() {
        UUID orderId = UUID.randomUUID();
        OrderEntity entity = OrderEntity.builder()
                .id(orderId)
                .customerId("cust-42")
                .sourceCurrency("EUR")
                .targetCurrency("USD")
                .sourceSubtotal(new BigDecimal("350.00"))
                .appliedExchangeRate(new BigDecimal("1.100000"))
                .rateSource(FxRateResult.SOURCE_LIVE_API)
                .convertedSubtotal(new BigDecimal("385.00"))
                .feePercentage(new BigDecimal("0.0150"))
                .feeAmount(new BigDecimal("5.78"))
                .netTotal(new BigDecimal("390.78"))
                .createdAt(OffsetDateTime.now())
                .build();

        OrderItemEntity item = OrderItemEntity.builder()
                .id(1L)
                .description("Keychron K2 Keyboard")
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .lineTotal(new BigDecimal("200.00"))
                .build();
        entity.addItem(item);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(entity));

        OrderResponse response = orderService.getOrder(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.orderId());
        assertEquals(1, response.items().size());
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when order does not exist")
    void testGetOrderById_NotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrder(orderId));
    }

    @Test
    @DisplayName("Retrieves aggregated metrics for customer")
    void testGetCustomerSummary() {
        CustomerSummaryProjection projection = mock(CustomerSummaryProjection.class);
        when(projection.getTotalOrders()).thenReturn(4L);
        when(projection.getTotalSpend()).thenReturn(new BigDecimal("1250.80"));
        when(projection.getCumulativeFeesPaid()).thenReturn(new BigDecimal("18.75"));

        when(orderRepository.findSummaryByCustomerId("cust-42")).thenReturn(projection);

        CustomerSummaryResponse summary = orderService.getCustomerSummary("cust-42");

        assertNotNull(summary);
        assertEquals("cust-42", summary.customerId());
        assertEquals(4L, summary.totalOrders());
        assertEquals(new BigDecimal("1250.80"), summary.totalSpend());
        assertEquals(new BigDecimal("18.75"), summary.cumulativeFeesPaid());
    }
}
