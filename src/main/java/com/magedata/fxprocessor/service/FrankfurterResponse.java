package com.magedata.fxprocessor.service;

import java.math.BigDecimal;

public class FrankfurterResponse {

    private String date;
    private String base;
    private String quote;
    private BigDecimal rate;

    public FrankfurterResponse() {
    }

    public FrankfurterResponse(String date, String base, String quote, BigDecimal rate) {
        this.date = date;
        this.base = base;
        this.quote = quote;
        this.rate = rate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
