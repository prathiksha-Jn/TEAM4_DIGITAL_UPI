package com.UPI;

import com.UPI.CLI.UserCli;
import com.UPI.repository.UserRepo;
import com.UPI.repository.WalletRepo;
import com.UPI.repository.TransactionRepo;
import com.UPI.services.UserService;
import com.UPI.services.WalletService;
import com.UPI.services.MoneyTransferService;

import java.util.Scanner;

public class main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Shared repositories
        UserRepo userRepo = new UserRepo();
        WalletRepo walletRepo = new WalletRepo();
        TransactionRepo transactionRepo = new TransactionRepo();

        // Services
        UserService userService = new UserService(userRepo);
        WalletService walletService = new WalletService(transactionRepo);
        MoneyTransferService transferService =
                new MoneyTransferService(userRepo, walletRepo, transactionRepo);

        System.out.println("===== DIGITAL WALLET AND UPI =====");

        UserCli userCli = new UserCli(
            sc, userService, walletService, walletRepo, transferService, transactionRepo
        );
        userCli.showMenu();

        sc.close();
    }
}
