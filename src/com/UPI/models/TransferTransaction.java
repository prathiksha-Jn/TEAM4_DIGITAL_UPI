package com.UPI.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransferTransaction {

    public enum Status {
        SUCCESS, FAILED, REVERSED
    }

    private String referenceId;
    private String senderPhone;
    private String receiverPhone;
    private double amount;
    private LocalDateTime timestamp;
    private Status status;

    public TransferTransaction(String senderPhone, String receiverPhone,
                               double amount, Status status) {
        this.referenceId = UUID.randomUUID().toString();
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}