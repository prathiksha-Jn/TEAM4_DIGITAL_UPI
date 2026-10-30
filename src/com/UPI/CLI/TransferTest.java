package com.UPI.CLI;

import com.UPI.models.User;
import com.UPI.models.TransferTransaction;
import com.UPI.repository.UserRepo;
import com.UPI.services.MoneyTransferService;

public class TransferTest {

    public static void main(String[] args) {

        UserRepo repo = new UserRepo();

        User sender = new User("Priyanka", "9876543210",
                "priya@gmail.com", "1234");

        User receiver = new User("Nithya", "9876543211",
                "nithya@gmail.com", "5678");

        repo.saveUser(sender);
        repo.saveUser(receiver);

        sender.credit(5000);

        MoneyTransferService service = new MoneyTransferService(repo);

        try {
            TransferTransaction transaction = service.transfer(
                    "9876543210", "9876543211", 1000, "1234");

            System.out.println("Transfer Status: " + transaction.getStatus());
            System.out.println("Reference ID: " + transaction.getReferenceId());
            System.out.println("Amount: Rs." + transaction.getAmount());

            System.out.println("Sender Balance: Rs."
                    + sender.getWalletBalance());

            System.out.println("Receiver Balance: Rs."
                    + receiver.getWalletBalance());

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}