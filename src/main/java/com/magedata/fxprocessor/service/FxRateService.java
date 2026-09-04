package com.magedata.fxprocessor.service;

public interface FxRateService {

    FxRateResult getExchangeRate(String sourceCurrency, String targetCurrency);
}
