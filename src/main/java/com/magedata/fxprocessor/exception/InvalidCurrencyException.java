package com.magedata.fxprocessor.exception;

public class InvalidCurrencyException extends FxRateUnavailableException {

    public InvalidCurrencyException(String message) {
        super(message);
    }
}
