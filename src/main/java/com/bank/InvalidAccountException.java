package com.bank;

public class InvalidAccountException extends BankException {
    public InvalidAccountException(long acno) {
        super("Account " + acno + " was not found.");
    }
}
