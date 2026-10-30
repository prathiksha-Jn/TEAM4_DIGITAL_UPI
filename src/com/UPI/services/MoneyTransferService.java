package com.UPI.services;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.UPI.models.User;
import com.UPI.models.UserStatus;
import com.UPI.models.TransferTransaction;
import com.UPI.models.TransferTransaction.Status;
import com.UPI.repository.UserRepo;

public class MoneyTransferService {

    private UserRepo userRepo;

    private List<TransferTransaction> transactions = new ArrayList<>();

    private Map<String, Integer> wrongAttempts = new HashMap<>();
    private Map<String, Instant> lockedUntil = new HashMap<>();
    private Map<String, Double> dailyTransfers = new HashMap<>();

    public MoneyTransferService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public synchronized TransferTransaction transfer(
            String senderPhone, String receiverPhone,
            double amount, String pin) {

        User sender = userRepo.findByPhoneNumber(senderPhone);
        User receiver = userRepo.findByPhoneNumber(receiverPhone);

        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("Sender or receiver not found");
        }

        if (senderPhone.equals(receiverPhone)) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        if (sender.getUserStatus() != UserStatus.ACTIVE ||
            receiver.getUserStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Both users must be active");
        }

        if (amount < 1 || amount > 10000) {
            throw new IllegalArgumentException(
                "Transfer amount must be between Rs.1 and Rs.10,000");
        }

        Instant now = Instant.now();

        if (lockedUntil.containsKey(senderPhone)) {
            if (now.isBefore(lockedUntil.get(senderPhone))) {
                throw new IllegalArgumentException(
                    "Transactions locked for 24 hours");
            }

            lockedUntil.remove(senderPhone);
            wrongAttempts.remove(senderPhone);
        }

        if (!sender.verifyPin(pin)) {
            int attempts = wrongAttempts.getOrDefault(senderPhone, 0) + 1;
            wrongAttempts.put(senderPhone, attempts);

            if (attempts >= 3) {
                lockedUntil.put(senderPhone, now.plusSeconds(86400));

                throw new IllegalArgumentException(
                    "3 wrong attempts! Transactions locked for 24 hours");
            }

            throw new IllegalArgumentException(
                "Incorrect PIN. Attempts remaining: " + (3 - attempts));
        }

        wrongAttempts.remove(senderPhone);

        String dailyKey = senderPhone + "_" + LocalDate.now();

        double alreadySent = dailyTransfers.getOrDefault(dailyKey, 0.0);

        if (alreadySent + amount > 25000) {
            throw new IllegalArgumentException(
                "Daily transfer limit of Rs.25,000 exceeded");
        }

        if (sender.getWalletBalance() < amount) {

            TransferTransaction failed = new TransferTransaction(
                senderPhone, receiverPhone, amount, Status.FAILED);

            transactions.add(failed);

            return failed;
        }

        sender.debit(amount);
        receiver.credit(amount);

        TransferTransaction success = new TransferTransaction(
            senderPhone, receiverPhone, amount, Status.SUCCESS);

        transactions.add(success);

        dailyTransfers.put(dailyKey, alreadySent + amount);

        return success;
    }

    public List<TransferTransaction> getTransactions() {
        return transactions;
    }
}