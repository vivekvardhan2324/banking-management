package com.bank;

public class InsufficientFundsException extends BankException {
    public InsufficientFundsException() {
        super("Insufficient funds for this withdrawal.");
    }
}
