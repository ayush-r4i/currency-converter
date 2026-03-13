package com.currencyconverter.util;

import com.currencyconverter.model.Conversion;

import java.util.List;

/**
 * Minimal JSON builder — no external library needed.
 */
public class JsonUtil {

    private JsonUtil() {}

    /** Serialize a single Conversion to JSON. */
    public static String toJson(Conversion c) {
        return String.format(
                "{\"amount\":%.4f,\"fromCurrency\":\"%s\",\"toCurrency\":\"%s\"," +
                "\"result\":%.4f,\"timestamp\":%d}",
                c.getAmount(),
                escape(c.getFromCurrency()),
                escape(c.getToCurrency()),
                c.getResult(),
                c.getTimestamp()
        );
    }

    /** Serialize a list of Conversions to a JSON array. */
    public static String toJson(List<Conversion> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parse a JSON body that contains amount, fromCurrency, toCurrency.
     * Very lightweight — reads only the three keys we care about.
     */
    public static ConvertRequest parseConvertRequest(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Empty request body");
        }
        double amount       = parseDouble(json, "amount");
        String fromCurrency = parseString(json, "fromCurrency");
        String toCurrency   = parseString(json, "toCurrency");
        return new ConvertRequest(amount, fromCurrency, toCurrency);
    }

    // ── Helpers ──────────────────────────────────────────

    private static double parseDouble(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) throw new IllegalArgumentException("Missing field: " + key);
        int colon = json.indexOf(':', idx + search.length());
        StringBuilder num = new StringBuilder();
        for (int i = colon + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ' ' || c == '\t') continue;
            if (c == ',' || c == '}') break;
            num.append(c);
        }
        try {
            return Double.parseDouble(num.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for field: " + key);
        }
    }

    private static String parseString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) throw new IllegalArgumentException("Missing field: " + key);
        int colon  = json.indexOf(':', idx + search.length());
        int open   = json.indexOf('"', colon + 1);
        int close  = json.indexOf('"', open + 1);
        if (open == -1 || close == -1) throw new IllegalArgumentException("Invalid string for field: " + key);
        return json.substring(open + 1, close);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ── Inner DTO ─────────────────────────────────────────

    public static class ConvertRequest {
        public final double amount;
        public final String fromCurrency;
        public final String toCurrency;

        ConvertRequest(double amount, String fromCurrency, String toCurrency) {
            this.amount       = amount;
            this.fromCurrency = fromCurrency;
            this.toCurrency   = toCurrency;
        }
    }
}
