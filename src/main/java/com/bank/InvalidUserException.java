package com.bank;

public class InvalidUserException extends BankException {
    public InvalidUserException() {
        super("Invalid user ID or password.");
    }
}
