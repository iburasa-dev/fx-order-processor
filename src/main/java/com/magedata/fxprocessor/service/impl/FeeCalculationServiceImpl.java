package com.magedata.fxprocessor.service.impl;

import com.magedata.fxprocessor.service.FeeCalculationService;
import com.magedata.fxprocessor.service.FeeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FeeCalculationServiceImpl implements FeeCalculationService {

    private final BigDecimal thresholdUsd;
    private final BigDecimal underThresholdPercent;
    private final BigDecimal aboveThresholdPercent;

    public FeeCalculationServiceImpl(
            @Value("${fx.fee.threshold-usd:1000.00}") BigDecimal thresholdUsd,
            @Value("${fx.fee.under-threshold-percent:0.0150}") BigDecimal underThresholdPercent,
            @Value("${fx.fee.above-threshold-percent:0.0050}") BigDecimal aboveThresholdPercent) {
        this.thresholdUsd = thresholdUsd;
        this.underThresholdPercent = underThresholdPercent;
        this.aboveThresholdPercent = aboveThresholdPercent;
    }

    @Override
    public FeeResult calculateFee(BigDecimal convertedSubtotal, BigDecimal subtotalInUsd) {
        BigDecimal feePercent;
        String tierDescription;

        if (subtotalInUsd.compareTo(thresholdUsd) < 0) {
            feePercent = underThresholdPercent;
            tierDescription = "Tier 1: 1.5% fee for orders under $1,000 USD";
        } else {
            feePercent = aboveThresholdPercent;
            tierDescription = "Tier 2: 0.5% fee for orders $1,000 USD and above";
        }

        BigDecimal feeAmount = convertedSubtotal.multiply(feePercent).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netTotal = convertedSubtotal.add(feeAmount).setScale(2, RoundingMode.HALF_UP);

        return new FeeResult(feePercent, feeAmount, netTotal, tierDescription);
    }
}
