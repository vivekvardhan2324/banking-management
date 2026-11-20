package com.bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class BankDatabase {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/banking_management";

    private BankDatabase() {
    }

    public static Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("MYSQL_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("MYSQL_USER", "root");
        String password = System.getenv().getOrDefault("MYSQL_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    public static void initialize() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS bank_users (
                        user_id VARCHAR(64) PRIMARY KEY,
                        password_salt VARCHAR(64) NOT NULL,
                        password_hash VARCHAR(128) NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Customers (
                        acno BIGINT PRIMARY KEY AUTO_INCREMENT,
                        cname VARCHAR(120) NOT NULL,
                        email VARCHAR(254) NOT NULL,
                        accounttype VARCHAR(32) NOT NULL,
                        bal DECIMAL(15, 2) NOT NULL,
                        owner_user_id VARCHAR(64) NOT NULL,
                        CONSTRAINT fk_customers_user
                            FOREIGN KEY (owner_user_id) REFERENCES bank_users(user_id)
                            ON DELETE CASCADE
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Transactions (
                        tid BIGINT PRIMARY KEY AUTO_INCREMENT,
                        transaction_type CHAR(1) NOT NULL,
                        acno BIGINT NOT NULL,
                        amount DECIMAL(15, 2) NOT NULL,
                        transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_transactions_customer
                            FOREIGN KEY (acno) REFERENCES Customers(acno)
                            ON DELETE CASCADE
                    )
                    """);
        }
    }
}
