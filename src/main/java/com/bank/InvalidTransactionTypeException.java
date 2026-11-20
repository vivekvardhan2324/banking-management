package com.bank;

public class InvalidTransactionTypeException extends BankException {
    public InvalidTransactionTypeException(char transactionType) {
        super("Invalid transaction type '" + transactionType + "'. Use 'd' for deposit or 'w' for withdrawal.");
    }
}
