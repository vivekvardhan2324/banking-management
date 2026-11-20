package com.bank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankTransaction {
    private long tid;
    private char transactionType;
    private long acno;
    private BigDecimal amount;
    private LocalDateTime transactionDate;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public char getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(char transactionType) {
        this.transactionType = transactionType;
    }

    public long getAcno() {
        return acno;
    }

    public void setAcno(long acno) {
        this.acno = acno;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
