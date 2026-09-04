package com.magedata.fxprocessor.service;

import com.magedata.fxprocessor.service.impl.FeeCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FeeCalculationServiceTest {

    private FeeCalculationService feeCalculationService;

    @BeforeEach
    void setUp() {
        feeCalculationService = new FeeCalculationServiceImpl(
                new BigDecimal("1000.00"),
                new BigDecimal("0.0150"),
                new BigDecimal("0.0050")
        );
    }

    @Test
    @DisplayName("Subtotal under $1,000 USD incurs 1.5% fee")
    void testCalculateFee_UnderThreshold() {
        BigDecimal convertedSubtotal = new BigDecimal("500.00");
        BigDecimal subtotalInUsd = new BigDecimal("500.00");

        FeeResult result = feeCalculationService.calculateFee(convertedSubtotal, subtotalInUsd);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.0150"), result.feePercentage());
        assertEquals(new BigDecimal("7.50"), result.feeAmount());
        assertEquals(new BigDecimal("507.50"), result.netTotal());
        assertTrue(result.tierDescription().contains("1.5%"));
    }

    @Test
    @DisplayName("Subtotal equal to $1,000 USD incurs volume 0.5% fee")
    void testCalculateFee_ExactlyAtThreshold() {
        BigDecimal convertedSubtotal = new BigDecimal("1000.00");
        BigDecimal subtotalInUsd = new BigDecimal("1000.00");

        FeeResult result = feeCalculationService.calculateFee(convertedSubtotal, subtotalInUsd);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.0050"), result.feePercentage());
        assertEquals(new BigDecimal("5.00"), result.feeAmount());
        assertEquals(new BigDecimal("1005.00"), result.netTotal());
        assertTrue(result.tierDescription().contains("0.5%"));
    }

    @Test
    @DisplayName("Subtotal above $1,000 USD incurs volume 0.5% fee")
    void testCalculateFee_AboveThreshold() {
        BigDecimal convertedSubtotal = new BigDecimal("2500.00");
        BigDecimal subtotalInUsd = new BigDecimal("2500.00");

        FeeResult result = feeCalculationService.calculateFee(convertedSubtotal, subtotalInUsd);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.0050"), result.feePercentage());
        assertEquals(new BigDecimal("12.50"), result.feeAmount());
        assertEquals(new BigDecimal("2512.50"), result.netTotal());
    }

    @Test
    @DisplayName("Proper HALF_UP rounding for fractional fee amounts")
    void testCalculateFee_RoundingHalfUp() {
        // 123.45 * 0.015 = 1.85175 -> rounds to 1.85
        BigDecimal convertedSubtotal = new BigDecimal("123.45");
        BigDecimal subtotalInUsd = new BigDecimal("123.45");

        FeeResult result = feeCalculationService.calculateFee(convertedSubtotal, subtotalInUsd);

        assertEquals(new BigDecimal("1.85"), result.feeAmount());
        assertEquals(new BigDecimal("125.30"), result.netTotal());
    }
}
