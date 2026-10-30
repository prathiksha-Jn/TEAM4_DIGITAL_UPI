
package com.UPI.models;

public enum KycStatus {

    BASIC(10000, 20000.0),
    FULL(100000, null);

    private final double walletLimit;
    private final Double monthlyTransferLimit;

    KycStatus(double walletLimit, Double monthlyTransferLimit) {
        this.walletLimit = walletLimit;
        this.monthlyTransferLimit = monthlyTransferLimit;
    }

    public double getWalletLimit() {
        return walletLimit;
    }

    public Double getMonthlyTransferLimit() {
        return monthlyTransferLimit;
    }
}
