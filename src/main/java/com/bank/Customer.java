package com.bank;

import java.math.BigDecimal;

public class Customer {
    private long acno;
    private String cname;
    private String email;
    private String accounttype;
    private BigDecimal bal;

    public long getAcno() {
        return acno;
    }

    public void setAcno(long acno) {
        this.acno = acno;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccounttype() {
        return accounttype;
    }

    public void setAccounttype(String accounttype) {
        this.accounttype = accounttype;
    }

    public BigDecimal getBal() {
        return bal;
    }

    public void setBal(BigDecimal bal) {
        this.bal = bal;
    }

    @Override
    public String toString() {
        return "Account number: " + acno
                + ", Name: " + cname
                + ", Email: " + email
                + ", Account type: " + accounttype
                + ", Balance: " + bal.toPlainString();
    }
}
