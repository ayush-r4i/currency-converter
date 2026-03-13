package com.currencyconverter.servlet;

import com.currencyconverter.model.Conversion;
import com.currencyconverter.util.ConversionStore;
import com.currencyconverter.util.ExchangeRateClient;
import com.currencyconverter.util.JsonUtil;
import com.currencyconverter.util.JsonUtil.ConvertRequest;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * POST /convert
 *
 * Request body (JSON):
 * {
 *   "amount": 100,
 *   "fromCurrency": "USD",
 *   "toCurrency": "INR"
 * }
 *
 * Response (JSON):
 * {
 *   "amount": 100.0000,
 *   "fromCurrency": "USD",
 *   "toCurrency": "INR",
 *   "result": 8312.5000,
 *   "timestamp": 1712345678000
 * }
 */
@WebServlet("/convert")
public class ConvertServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ConvertServlet.class.getName());

    // ── CORS pre-flight ───────────────────────────────────
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    // ── POST /convert ─────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        // Read body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        // Parse
        ConvertRequest cr;
        try {
            cr = JsonUtil.parseConvertRequest(sb.toString());
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }

        // Validate
        if (cr.amount <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Amount must be greater than 0\"}");
            return;
        }
        if (cr.fromCurrency == null || cr.fromCurrency.isBlank() ||
            cr.toCurrency   == null || cr.toCurrency.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"fromCurrency and toCurrency are required\"}");
            return;
        }
        if (cr.fromCurrency.equalsIgnoreCase(cr.toCurrency)) {
            // Same currency → rate is 1
        }

        // Fetch rate & calculate
        double rate;
        try {
            rate = ExchangeRateClient.getRate(cr.fromCurrency, cr.toCurrency);
        } catch (RuntimeException e) {
            LOG.severe("Rate fetch failed: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }

        double result = cr.amount * rate;
        long   now    = System.currentTimeMillis();

        Conversion conversion = new Conversion(
                cr.amount,
                cr.fromCurrency.toUpperCase(),
                cr.toCurrency.toUpperCase(),
                result,
                now
        );

        ConversionStore.add(conversion);
        LOG.info("Converted: " + conversion);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(JsonUtil.toJson(conversion));
    }

    // ── CORS ─────────────────────────────────────────────
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
