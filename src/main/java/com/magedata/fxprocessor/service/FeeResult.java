package com.magedata.fxprocessor.service;

import java.math.BigDecimal;

public record FeeResult(
        BigDecimal feePercentage,
        BigDecimal feeAmount,
        BigDecimal netTotal,
        String tierDescription
) {}
