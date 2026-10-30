package com.UPI.services;

import com.UPI.models.User;
import com.UPI.models.UserStatus;
import com.UPI.models.Wallet;
import com.UPI.models.BankAccount;
import com.UPI.models.Transaction;

import com.UPI.repository.TransactionRepo;

import java.math.BigDecimal;
import java.util.*;
public class WalletService {

    private TransactionRepo transactionRepo;
    private List<Transaction> transactions = new ArrayList<>();

    public WalletService() {
        this(new TransactionRepo());
    }

    public WalletService(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    // Add Money
    public Transaction addMoney(User user, Wallet wallet,
            BankAccount bank, BigDecimal amount) {

        if (user.getUserStatus() != UserStatus.ACTIVE)
            throw new IllegalArgumentException("User is blocked");

        if (amount.compareTo(new BigDecimal("10")) < 0 ||
            amount.compareTo(new BigDecimal("50000")) > 0)
            throw new IllegalArgumentException("Invalid amount");

        if (bank.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient bank balance");

        if (wallet.getBalance().add(amount).compareTo(
                BigDecimal.valueOf(user.getWalletLimit())) > 0)
            throw new IllegalArgumentException("KYC limit exceeded");

        bank.setBalance(bank.getBalance().subtract(amount));
        wallet.setBalance(wallet.getBalance().add(amount));

        return saveTransaction(user, wallet, bank, "ADD_MONEY", amount);
    }

    // Withdraw Money
    public Transaction withdrawMoney(User user, Wallet wallet,
            BankAccount bank, BigDecimal amount) {

        if (user.getUserStatus() != UserStatus.ACTIVE)
            throw new IllegalArgumentException("User is blocked");

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Invalid amount");

        if (wallet.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient wallet balance");

        wallet.setBalance(wallet.getBalance().subtract(amount));
        bank.setBalance(bank.getBalance().add(amount));

        return saveTransaction(user, wallet, bank, "WITHDRAW", amount);
    }

    // Save Transaction
    private Transaction saveTransaction(User user, Wallet wallet,
            BankAccount bank, String type, BigDecimal amount) {

        Transaction t = new Transaction(
            UUID.randomUUID().toString(),
            user.getUserId(),
            wallet.getWalletId(),
            bank.getAccountId(),
            type, amount, "SUCCESS"
        );

        transactions.add(t);
        if (transactionRepo != null) {
            transactionRepo.save(t);
        }
        return t;
    }

    public List<Transaction> getTransactions() {
        if (transactionRepo != null) {
            return transactionRepo.getAll();
        }
        return Collections.unmodifiableList(transactions);
    }
}
