package com.UPI.repository;

import com.UPI.models.Transaction;
import java.util.*;

public class TransactionRepo {

    private List<Transaction> transactions = new ArrayList<>();

    // Save transaction
    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    // Get all transactions
    public List<Transaction> getAll() {
        return Collections.unmodifiableList(transactions);
    }
}
