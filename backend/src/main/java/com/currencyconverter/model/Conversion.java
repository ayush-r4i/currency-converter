package com.currencyconverter.model;

/**
 * Represents a single currency conversion record.
 */
public class Conversion {

    private double amount;
    private String fromCurrency;
    private String toCurrency;
    private double result;
    private long   timestamp;   // epoch millis

    public Conversion() {}

    public Conversion(double amount, String fromCurrency, String toCurrency,
                      double result, long timestamp) {
        this.amount       = amount;
        this.fromCurrency = fromCurrency;
        this.toCurrency   = toCurrency;
        this.result       = result;
        this.timestamp    = timestamp;
    }

    // ── Getters ──────────────────────────────────────────

    public double getAmount()       { return amount; }
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency()   { return toCurrency; }
    public double getResult()       { return result; }
    public long   getTimestamp()    { return timestamp; }

    // ── Setters ──────────────────────────────────────────

    public void setAmount(double amount)             { this.amount = amount; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    public void setToCurrency(String toCurrency)     { this.toCurrency = toCurrency; }
    public void setResult(double result)             { this.result = result; }
    public void setTimestamp(long timestamp)         { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("Conversion{%.4f %s -> %s = %.4f @ %d}",
                amount, fromCurrency, toCurrency, result, timestamp);
    }
}
