package com.UPI.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.UPI.models.User;
import com.UPI.models.UserStatus;
import com.UPI.models.Wallet;
import com.UPI.models.Transaction;
import com.UPI.repository.UserRepo;
import com.UPI.repository.WalletRepo;
import com.UPI.repository.TransactionRepo;

public class MoneyTransferService {

    private UserRepo userRepo;
    private WalletRepo walletRepo;
    private TransactionRepo transactionRepo;

    private Map<String, Integer> wrongAttempts = new HashMap<>();
    private Map<String, Instant> lockedUntil = new HashMap<>();
    private Map<String, BigDecimal> dailyTransfers = new HashMap<>();
    private Map<String, BigDecimal> monthlyTransfers = new HashMap<>();

    public MoneyTransferService(UserRepo userRepo) {
        this(userRepo, new WalletRepo(), new TransactionRepo());
    }

    public MoneyTransferService(UserRepo userRepo,
                                WalletRepo walletRepo,
                                TransactionRepo transactionRepo) {
        this.userRepo = userRepo;
        this.walletRepo = walletRepo;
        this.transactionRepo = transactionRepo;
    }

    public synchronized Transaction transfer(
            String senderPhone, String receiverPhone,
            double amount, String pin) {
        return transfer(senderPhone, receiverPhone, BigDecimal.valueOf(amount), pin);
    }

    public synchronized Transaction transfer(
            String senderPhone, String receiverPhone,
            BigDecimal amount, String pin) {

        if (senderPhone == null || senderPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender phone number cannot be empty");
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Receiver phone number cannot be empty");
        }

        User sender = userRepo.findByPhoneNumber(senderPhone.trim());
        User receiver = userRepo.findByPhoneNumber(receiverPhone.trim());

        if (sender == null) {
            throw new IllegalArgumentException("Sender not found");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver not found");
        }

        if (senderPhone.trim().equals(receiverPhone.trim()) ||
            sender.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        if (sender.getUserStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Sender is not active");
        }
        if (receiver.getUserStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Receiver is not active");
        }

        if (amount == null || amount.compareTo(BigDecimal.ONE) < 0 ||
            amount.compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("Transfer amount must be between Rs.1 and Rs.10,000");
        }

        Instant now = Instant.now();

        if (lockedUntil.containsKey(senderPhone)) {
            if (now.isBefore(lockedUntil.get(senderPhone))) {
                throw new IllegalArgumentException(
                    "Transactions locked for 24 hours due to 3 incorrect PIN attempts");
            }
            lockedUntil.remove(senderPhone);
            wrongAttempts.remove(senderPhone);
        }

        if (pin == null || !sender.verifyPin(pin)) {
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

        // Daily transfer limit: Rs.25,000 per sender
        String dailyKey = senderPhone + "_" + LocalDate.now();
        BigDecimal alreadySentDaily = dailyTransfers.getOrDefault(dailyKey, BigDecimal.ZERO);

        if (alreadySentDaily.add(amount).compareTo(new BigDecimal("25000")) > 0) {
            throw new IllegalArgumentException("Daily transfer limit of Rs.25,000 exceeded");
        }

        // Basic KYC monthly transfer limit
        Double monthlyLimit = sender.getMonthlyTransferLimit();
        String monthlyKey = senderPhone + "_" + LocalDate.now().getYear() + "_" + LocalDate.now().getMonthValue();
        BigDecimal alreadySentMonthly = monthlyTransfers.getOrDefault(monthlyKey, BigDecimal.ZERO);

        if (monthlyLimit != null) {
            BigDecimal limitBd = BigDecimal.valueOf(monthlyLimit);
            if (alreadySentMonthly.add(amount).compareTo(limitBd) > 0) {
                String limitFormatted = (monthlyLimit == monthlyLimit.longValue()) ?
                        String.valueOf(monthlyLimit.longValue()) : limitBd.toPlainString();
                throw new IllegalArgumentException(
                    "Monthly transfer limit of Rs." + limitFormatted + " exceeded for " + sender.getKycStatus() + " KYC");
            }
        }

        // Retrieve wallets from shared WalletRepo
        Wallet senderWallet = walletRepo.findByUserId(sender.getUserId());
        Wallet receiverWallet = walletRepo.findByUserId(receiver.getUserId());

        if (senderWallet == null) {
            senderWallet = new Wallet(UUID.randomUUID().toString(), sender.getUserId());
            walletRepo.save(senderWallet);
        }
        if (receiverWallet == null) {
            receiverWallet = new Wallet(UUID.randomUUID().toString(), receiver.getUserId());
            walletRepo.save(receiverWallet);
        }

        BigDecimal senderBalance = senderWallet.getBalance();
        BigDecimal receiverBalance = receiverWallet.getBalance();

        // Check sufficient sender wallet balance
        if (senderBalance.compareTo(amount) < 0) {
            Transaction failed = new Transaction(
                UUID.randomUUID().toString(),
                sender.getUserId(),
                senderWallet.getWalletId(),
                receiver.getUserId(),
                senderPhone,
                receiverPhone,
                "TRANSFER",
                amount,
                "FAILED"
            );
            failed.setFailureReason("Insufficient wallet balance");
            if (transactionRepo != null) {
                transactionRepo.save(failed);
            }
            return failed;
        }

        // Respect receiver's KYC wallet balance limit
        BigDecimal receiverLimit = BigDecimal.valueOf(receiver.getWalletLimit());
        if (receiverBalance.add(amount).compareTo(receiverLimit) > 0) {
            throw new IllegalArgumentException("Receiver KYC wallet limit exceeded");
        }

        // Atomic transfer: debit sender and credit receiver
        senderWallet.setBalance(senderBalance.subtract(amount));

        try {
            receiverWallet.setBalance(receiverBalance.add(amount));
        } catch (RuntimeException e) {
            // Rollback on credit failure
            senderWallet.setBalance(senderBalance);
            Transaction failed = new Transaction(
                UUID.randomUUID().toString(),
                sender.getUserId(),
                senderWallet.getWalletId(),
                receiver.getUserId(),
                senderPhone,
                receiverPhone,
                "TRANSFER",
                amount,
                "FAILED"
            );
            failed.setFailureReason("Transfer execution error: " + e.getMessage());
            if (transactionRepo != null) {
                transactionRepo.save(failed);
            }
            throw e;
        }

        Transaction success = new Transaction(
            UUID.randomUUID().toString(),
            sender.getUserId(),
            senderWallet.getWalletId(),
            receiver.getUserId(),
            senderPhone,
            receiverPhone,
            "TRANSFER",
            amount,
            "SUCCESS"
        );

        if (transactionRepo != null) {
            transactionRepo.save(success);
        }

        dailyTransfers.put(dailyKey, alreadySentDaily.add(amount));
        monthlyTransfers.put(monthlyKey, alreadySentMonthly.add(amount));

        return success;
    }

    public List<Transaction> getTransactions() {
        if (transactionRepo != null) {
            return transactionRepo.getAll();
        }
        return Collections.emptyList();
    }

    public boolean isLocked(String senderPhone) {
        if (!lockedUntil.containsKey(senderPhone)) return false;
        if (Instant.now().isBefore(lockedUntil.get(senderPhone))) return true;
        lockedUntil.remove(senderPhone);
        wrongAttempts.remove(senderPhone);
        return false;
    }

    public void resetPinAttempts(String senderPhone) {
        wrongAttempts.remove(senderPhone);
        lockedUntil.remove(senderPhone);
    }
}
