package com.currencyconverter.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Fetches live exchange rates from ExchangeRate-API (free tier).
 *
 * Free-tier endpoint (no API key needed for basic lookup):
 *   https://open.er-api.com/v6/latest/{BASE}
 *
 * Response JSON snippet:
 * {
 *   "result": "success",
 *   "rates": { "INR": 83.12, "EUR": 0.92, ... }
 * }
 */
public class ExchangeRateClient {

    private static final Logger LOG = Logger.getLogger(ExchangeRateClient.class.getName());

    // Free, no-key-required endpoint from open.er-api.com
    private static final String API_URL =
            "https://open.er-api.com/v6/latest/%s";

    private ExchangeRateClient() {}

    /**
     * Returns the exchange rate for converting 1 unit of {@code from}
     * to the {@code to} currency.
     *
     * @throws RuntimeException if the API call fails or the currency is not found.
     */
    public static double getRate(String from, String to) {
        String urlStr = String.format(API_URL, from.toUpperCase());
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8_000);
            conn.setReadTimeout(8_000);

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("Exchange rate API returned HTTP " + status);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            conn.disconnect();

            String json = sb.toString();
            return parseRate(json, to.toUpperCase());

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            LOG.severe("Failed to fetch exchange rate: " + e.getMessage());
            throw new RuntimeException("Unable to fetch exchange rate: " + e.getMessage(), e);
        }
    }

    /**
     * Minimal hand-rolled JSON parser — no library dependency.
     * Searches for {@code "TO_CODE":RATE} in the "rates" object.
     */
    private static double parseRate(String json, String targetCode) {
        // Find the rates block
        int ratesIdx = json.indexOf("\"rates\"");
        if (ratesIdx == -1) {
            throw new RuntimeException("Unexpected API response — 'rates' key not found.");
        }

        // Search for the currency code key after "rates"
        String search = "\"" + targetCode + "\"";
        int keyIdx = json.indexOf(search, ratesIdx);
        if (keyIdx == -1) {
            throw new RuntimeException("Currency not found in rates: " + targetCode);
        }

        // Skip past the key, colon and whitespace to find the numeric value
        int colonIdx = json.indexOf(':', keyIdx + search.length());
        if (colonIdx == -1) {
            throw new RuntimeException("Malformed JSON around currency: " + targetCode);
        }

        // Extract characters until comma or closing brace
        StringBuilder numStr = new StringBuilder();
        for (int i = colonIdx + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') continue;
            if (c == ',' || c == '}') break;
            numStr.append(c);
        }

        try {
            return Double.parseDouble(numStr.toString().trim());
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("Could not parse rate value: '" + numStr + "'");
        }
    }
}
