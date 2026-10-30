package com.UPI.repository;
import com.UPI.models.Wallet;
import java.util.HashMap;
import java.util.Map;

public class WalletRepo {

    private Map<String, Wallet> wallets = new HashMap<>();

    public void save(Wallet wallet) {
        wallets.put(wallet.getUserId(), wallet);
    }

    public Wallet findByUserId(String userId) {
        return wallets.get(userId);
    }
}
