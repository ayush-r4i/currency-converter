package com.currencyconverter.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.currencyconverter.model.Conversion;

public class ConversionStore {

    private static final Logger LOG = Logger.getLogger(ConversionStore.class.getName());

    /** Save a conversion to MySQL */
    public static void add(Conversion c) {
        String sql = "INSERT INTO conversions (amount, from_currency, to_currency, result, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, c.getAmount());
            ps.setString(2, c.getFromCurrency());
            ps.setString(3, c.getToCurrency());
            ps.setDouble(4, c.getResult());
            ps.setLong(5,   c.getTimestamp());
            ps.executeUpdate();

        } catch (Exception e) {
            LOG.severe("Failed to save conversion: " + e.getMessage());
        }
    }

    /** Fetch last 5 conversions from MySQL, newest first */
    public static List<Conversion> getLastFive() {
        List<Conversion> list = new ArrayList<>();
        String sql = "SELECT * FROM conversions ORDER BY timestamp DESC LIMIT 5";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Conversion(
                        rs.getDouble("amount"),
                        rs.getString("from_currency"),
                        rs.getString("to_currency"),
                        rs.getDouble("result"),
                        rs.getLong("timestamp")
                ));
            }
        } catch (Exception e) {
            LOG.severe("Failed to fetch history: " + e.getMessage());
        }
        return list;
    }
}