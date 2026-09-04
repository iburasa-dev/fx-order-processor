package com.magedata.fxprocessor.service;

import java.math.BigDecimal;

public record FxRateResult(
        BigDecimal rate,
        String rateSource
) {
    public static final String SOURCE_IDENTITY = "IDENTITY";
    public static final String SOURCE_LIVE_API = "LIVE_API";
    public static final String SOURCE_CACHE = "CACHE";
    public static final String SOURCE_FALLBACK_SNAPSHOT = "FALLBACK_SNAPSHOT";
    public static final String SOURCE_FALLBACK_EMERGENCY = "FALLBACK_EMERGENCY";
}
