package com.magedata.fxprocessor.exception;

public class FxRateUnavailableException extends RuntimeException {
    public FxRateUnavailableException(String message) {
        super(message);
    }

    public FxRateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
