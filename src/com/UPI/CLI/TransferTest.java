package com.UPI.CLI;

import com.UPI.models.User;
import com.UPI.models.Wallet;
import com.UPI.models.Transaction;
import com.UPI.repository.UserRepo;
import com.UPI.repository.WalletRepo;
import com.UPI.repository.TransactionRepo;
import com.UPI.services.MoneyTransferService;

import java.math.BigDecimal;

public class TransferTest {

    public static void main(String[] args) {

        UserRepo userRepo = new UserRepo();
        WalletRepo walletRepo = new WalletRepo();
        TransactionRepo transactionRepo = new TransactionRepo();

        User sender = new User("Priyanka", "9876543210",
                "priya@gmail.com", "1234");

        User receiver = new User("Nithya", "9876543211",
                "nithya@gmail.com", "5678");

        userRepo.saveUser(sender);
        userRepo.saveUser(receiver);

        Wallet senderWallet = new Wallet("W1", sender.getUserId());
        senderWallet.setBalance(new BigDecimal("5000"));
        walletRepo.save(senderWallet);

        Wallet receiverWallet = new Wallet("W2", receiver.getUserId());
        walletRepo.save(receiverWallet);

        MoneyTransferService service = new MoneyTransferService(userRepo, walletRepo, transactionRepo);

        try {
            Transaction transaction = service.transfer(
                    "9876543210", "9876543211", new BigDecimal("1000"), "1234");

            System.out.println("Transfer Status: " + transaction.getStatus());
            System.out.println("Reference ID: " + transaction.getTransactionId());
            System.out.println("Amount: Rs." + transaction.getAmount());

            System.out.println("Sender Balance: Rs."
                    + senderWallet.getBalance());

            System.out.println("Receiver Balance: Rs."
                    + receiverWallet.getBalance());

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}