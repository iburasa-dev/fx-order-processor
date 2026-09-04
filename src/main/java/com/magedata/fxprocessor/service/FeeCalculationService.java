package com.magedata.fxprocessor.service;

import java.math.BigDecimal;

public interface FeeCalculationService {

    FeeResult calculateFee(BigDecimal convertedSubtotal, BigDecimal subtotalInUsd);
}
