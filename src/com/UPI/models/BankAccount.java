package com.UPI.models;

import java.math.BigDecimal;

public class BankAccount {

    private String accountId;
    private String userId;
    private String accountNumber;
    private String bankName;
    private BigDecimal balance;
    private boolean primary;

    // Constructor
    public BankAccount(String accountId, String userId,
                       String accountNumber, String bankName,
                       BigDecimal balance, boolean primary) {

        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        setBalance(balance);
        this.primary = primary;
    }

    // Getters
    public String getAccountId() {
        return accountId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isPrimary() {
        return primary;
    }

    // Setters
    public void setBalance(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid bank balance");
        }
        this.balance = balance;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
