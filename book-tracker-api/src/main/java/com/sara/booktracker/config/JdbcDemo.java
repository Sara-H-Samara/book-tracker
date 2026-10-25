package com.sara.booktracker.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class JdbcDemo {

    private final DataSource dataSource;

    public JdbcDemo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runDemo() throws SQLException {
        String sql = "SELECT id, title, pages FROM books WHERE pages > ? ORDER BY pages DESC";

        System.out.println("===== JDBC DEMO =====");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, 300);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String title = rs.getString("title");
                    int pages = rs.getInt("pages");

                    System.out.println("📖 " + id + " | " + title + " | " + pages + " pages");
                }
            }
        }

        System.out.println("=====================");
    }
}