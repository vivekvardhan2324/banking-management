package com.bank;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BankService {
    private static final BigDecimal MIN_WITHDRAWAL = new BigDecimal("100.00");
    private static final BigDecimal MAX_DEPOSIT = new BigDecimal("50000.00");
    private final String userId;

    public BankService(String userId) {
        this.userId = userId;
    }

    public Customer createAccount(Customer customer) throws SQLException, InvalidAmountException {
        if (customer.getBal() == null || customer.getBal().signum() < 0) {
            throw new InvalidAmountException("Opening balance cannot be negative.");
        }
        String sql = """
                INSERT INTO Customers (cname, email, accounttype, bal, owner_user_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = BankDatabase.connect();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getCname());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getAccounttype());
            statement.setBigDecimal(4, customer.getBal());
            statement.setString(5, userId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Account was created but its generated account number was not returned.");
                }
                customer.setAcno(keys.getLong(1));
            }
        }
        return customer;
    }

    public List<Customer> viewAccounts() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT acno, cname, email, accounttype, bal FROM Customers "
                + "WHERE owner_user_id = ? ORDER BY acno";
        try (Connection connection = BankDatabase.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    customers.add(readCustomer(results));
                }
            }
        }
        return customers;
    }

    public Customer findAccount(long acno) throws SQLException, InvalidAccountException {
        String sql = "SELECT acno, cname, email, accounttype, bal FROM Customers "
                + "WHERE acno = ? AND owner_user_id = ?";
        try (Connection connection = BankDatabase.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, acno);
            statement.setString(2, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return readCustomer(results);
                }
            }
        }
        throw new InvalidAccountException(acno);
    }

    public BigDecimal deposit(long acno, BigDecimal amount)
            throws SQLException, InvalidAccountException, InvalidAmountException, InsufficientFundsException {
        validateDeposit(amount);
        return changeBalance(acno, amount, 'd');
    }

    public BigDecimal withdraw(long acno, BigDecimal amount)
            throws SQLException, InvalidAccountException, InvalidAmountException, InsufficientFundsException {
        if (amount == null || amount.compareTo(MIN_WITHDRAWAL) < 0) {
            throw new InvalidAmountException("Withdrawal amount must be at least 100.00.");
        }
        return changeBalance(acno, amount.negate(), 'w');
    }

    public BigDecimal performTransaction(long acno, char transactionType, BigDecimal amount)
            throws SQLException, InvalidAccountException, InvalidAmountException,
            InsufficientFundsException, InvalidTransactionTypeException {
        return switch (Character.toLowerCase(transactionType)) {
            case 'd' -> deposit(acno, amount);
            case 'w' -> withdraw(acno, amount);
            default -> throw new InvalidTransactionTypeException(transactionType);
        };
    }

    public void deleteAccount(long acno) throws SQLException, InvalidAccountException {
        String sql = "DELETE FROM Customers WHERE acno = ? AND owner_user_id = ?";
        try (Connection connection = BankDatabase.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, acno);
            statement.setString(2, userId);
            if (statement.executeUpdate() == 0) {
                throw new InvalidAccountException(acno);
            }
        }
    }

    private BigDecimal changeBalance(long acno, BigDecimal delta, char transactionType)
            throws SQLException, InvalidAccountException, InsufficientFundsException {
        String findSql = "SELECT bal FROM Customers WHERE acno = ? AND owner_user_id = ? FOR UPDATE";
        try (Connection connection = BankDatabase.connect()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal balance;
                try (PreparedStatement statement = connection.prepareStatement(findSql)) {
                    statement.setLong(1, acno);
                    statement.setString(2, userId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new InvalidAccountException(acno);
                        }
                        balance = result.getBigDecimal("bal");
                    }
                }

                BigDecimal updatedBalance = balance.add(delta);
                if (updatedBalance.signum() < 0) {
                    throw new InsufficientFundsException();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE Customers SET bal = ? WHERE acno = ? AND owner_user_id = ?")) {
                    statement.setBigDecimal(1, updatedBalance);
                    statement.setLong(2, acno);
                    statement.setString(3, userId);
                    if (statement.executeUpdate() != 1) {
                        throw new InvalidAccountException(acno);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO Transactions (transaction_type, acno, amount) VALUES (?, ?, ?)")) {
                    statement.setString(1, String.valueOf(transactionType));
                    statement.setLong(2, acno);
                    statement.setBigDecimal(3, delta.abs());
                    statement.executeUpdate();
                }
                connection.commit();
                return updatedBalance;
            } catch (SQLException | InvalidAccountException | InsufficientFundsException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void validateDeposit(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.signum() <= 0 || amount.compareTo(MAX_DEPOSIT) > 0) {
            throw new InvalidAmountException("Deposit must be greater than zero and at most 50,000.00.");
        }
    }

    private Customer readCustomer(ResultSet results) throws SQLException {
        Customer customer = new Customer();
        customer.setAcno(results.getLong("acno"));
        customer.setCname(results.getString("cname"));
        customer.setEmail(results.getString("email"));
        customer.setAccounttype(results.getString("accounttype"));
        customer.setBal(results.getBigDecimal("bal"));
        return customer;
    }
}
