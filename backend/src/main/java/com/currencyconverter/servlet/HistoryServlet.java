package com.currencyconverter.servlet;

import com.currencyconverter.util.ConversionStore;
import com.currencyconverter.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * GET /history
 *
 * Returns the last 5 conversions as a JSON array:
 * [
 *   {
 *     "amount": 100.0000,
 *     "fromCurrency": "USD",
 *     "toCurrency": "INR",
 *     "result": 8312.5000,
 *     "timestamp": 1712345678000
 *   },
 *   ...
 * ]
 */
@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    // ── CORS pre-flight ───────────────────────────────────
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    // ── GET /history ──────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(JsonUtil.toJson(ConversionStore.getLastFive()));
    }

    // ── CORS ─────────────────────────────────────────────
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
