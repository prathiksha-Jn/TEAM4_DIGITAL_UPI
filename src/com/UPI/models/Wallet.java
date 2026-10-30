package com.UPI.models;

import java.math.BigDecimal;

public class Wallet {

    private String walletId;
    private String userId;
    private BigDecimal balance;

    // Constructor
    public Wallet(String walletId, String userId) {
        this.walletId = walletId;
        this.userId = userId;
        this.balance = BigDecimal.ZERO;
    }

    // Getters
    public String getWalletId() {
        return walletId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    // Setter
    public void setBalance(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid wallet balance");
        }
        this.balance = balance;
    }
}
