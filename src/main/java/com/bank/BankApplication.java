package com.bank;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class BankApplication {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthenticationService authenticationService = new AuthenticationService();

    public static void main(String[] args) {
        try {
            BankDatabase.initialize();
            new BankApplication().run();
        } catch (SQLException exception) {
            System.err.println("Could not initialize/connect to MySQL: " + exception.getMessage());
            System.exit(1);
        }
    }

    private void run() {
        while (true) {
            System.out.println("\n1. Sign up\n2. Sign in\n3. Exit");
            String choice = prompt("Choose an option: ");
            switch (choice) {
                case "1" -> signUp();
                case "2" -> signIn();
                case "3" -> {
                    System.out.println("Goodbye.");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void signUp() {
        String userId = prompt("User ID: ").trim();
        char[] password = prompt("Password: ").toCharArray();
        if (userId.isEmpty() || password.length == 0) {
            System.out.println("User ID and password are required.");
            return;
        }
        try {
            authenticationService.register(userId, password);
            System.out.println("Sign-up successful. You can now sign in.");
        } catch (SQLException exception) {
            System.out.println("Could not create user (the ID may already exist): " + exception.getMessage());
        } catch (GeneralSecurityException exception) {
            System.out.println("Could not securely process the password: " + exception.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void signIn() {
        String userId = prompt("User ID: ").trim();
        char[] password = prompt("Password: ").toCharArray();
        try {
            authenticationService.login(userId, password);
            System.out.println("Sign-in successful.");
            accountMenu(new BankService(userId));
        } catch (InvalidUserException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Could not sign in: " + exception.getMessage());
        } catch (GeneralSecurityException exception) {
            System.out.println("Could not verify credentials: " + exception.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void accountMenu(BankService bankService) {
        while (true) {
            System.out.println("""
                    
                    Bank Transactions
                    1. Create Account
                    2. View Accounts
                    3. Search Account
                    4. Deposit
                    5. Withdraw
                    6. Delete Account
                    7. Sign out
                    """);
            String choice = prompt("Choose an option: ");
            try {
                switch (choice) {
                    case "1" -> createAccount(bankService);
                    case "2" -> viewAccounts(bankService);
                    case "3" -> searchAccount(bankService);
                    case "4" -> deposit(bankService);
                    case "5" -> withdraw(bankService);
                    case "6" -> deleteAccount(bankService);
                    case "7" -> {
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (BankException | SQLException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private void createAccount(BankService bankService) throws SQLException, BankException {
        Customer customer = new Customer();
        customer.setCname(prompt("Customer name: ").trim());
        customer.setEmail(prompt("Email: ").trim());
        customer.setAccounttype(prompt("Account type (savings/current): ").trim());
        customer.setBal(readAmount("Opening balance: "));
        bankService.createAccount(customer);
        System.out.println("Account created:");
        System.out.println(customer);
    }

    private void viewAccounts(BankService bankService) throws SQLException {
        List<Customer> customers = bankService.viewAccounts();
        if (customers.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        customers.forEach(System.out::println);
    }

    private void searchAccount(BankService bankService) throws SQLException, BankException {
        System.out.println(bankService.findAccount(readAccountNumber()));
    }

    private void deposit(BankService bankService) throws SQLException, BankException {
        long acno = readAccountNumber();
        BigDecimal updatedBalance = bankService.performTransaction(
                acno, 'd', readAmount("Deposit amount: "));
        System.out.println("Deposit successful. New balance: " + updatedBalance.toPlainString());
    }

    private void withdraw(BankService bankService) throws SQLException, BankException {
        long acno = readAccountNumber();
        BigDecimal updatedBalance = bankService.performTransaction(
                acno, 'w', readAmount("Withdrawal amount: "));
        System.out.println("Withdrawal successful. New balance: " + updatedBalance.toPlainString());
    }

    private void deleteAccount(BankService bankService) throws SQLException, BankException {
        long acno = readAccountNumber();
        bankService.deleteAccount(acno);
        System.out.println("Account deleted.");
    }

    private long readAccountNumber() {
        while (true) {
            try {
                return Long.parseLong(prompt("Account number: ").trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid numeric account number.");
            }
        }
    }

    private BigDecimal readAmount(String label) {
        while (true) {
            try {
                return new BigDecimal(prompt(label).trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid amount.");
            }
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }
}
