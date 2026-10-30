package com.UPI.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private String transactionId;
    private String userId;
    private String walletId;
    private String accountId;

    private String transactionType;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    private LocalDateTime transactionDate;

    // Additional fields for money transfer
    private String receiverUserId;
    private String senderPhone;
    private String receiverPhone;

    // Constructor
    public Transaction(String transactionId, String userId,
                       String walletId, String accountId,
                       String transactionType, BigDecimal amount,
                       String status) {

        this.transactionId = transactionId;
        this.userId = userId;
        this.walletId = walletId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.status = status;
        this.failureReason = null;
        this.transactionDate = LocalDateTime.now();
    }

    // Overloaded Constructor for transfers
    public Transaction(String transactionId, String senderUserId,
                       String senderWalletId, String receiverUserId,
                       String senderPhone, String receiverPhone,
                       String transactionType, BigDecimal amount,
                       String status) {

        this.transactionId = transactionId;
        this.userId = senderUserId;
        this.walletId = senderWalletId;
        this.receiverUserId = receiverUserId;
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.accountId = null;
        this.transactionType = transactionType;
        this.amount = amount;
        this.status = status;
        this.failureReason = null;
        this.transactionDate = LocalDateTime.now();
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getReferenceId() {
        return transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSenderUserId() {
        return userId;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }
}
