
package com.UPI.CLI;

import com.UPI.models.*;
import com.UPI.services.WalletService;
import java.math.BigDecimal;
import java.util.Scanner;

public class WalletCLI {

    private Scanner sc;
    private WalletService service;
    private User user;
    private Wallet wallet;
    private BankAccount bank;

    public WalletCLI(Scanner sc, WalletService service,
                     User user, Wallet wallet, BankAccount bank) {
        this.sc = sc;
        this.service = service;
        this.user = user;
        this.wallet = wallet;
        this.bank = bank;
    }

    public void showMenu() {

        while (true) {
            System.out.println("\n1. Add Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Check Wallet Balance");
            System.out.println("4. Exit");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();

            if (choice == 4) return;

            if (choice == 3) {
                System.out.println("Wallet Balance: Rs." +
                        wallet.getBalance());
                continue;
            }

            System.out.print("Enter amount: ");
            BigDecimal amount = sc.nextBigDecimal();

            try {
                if (choice == 1) {
                    service.addMoney(user, wallet, bank, amount);
                    System.out.println("Money added successfully!");
                } else if (choice == 2) {
                    service.withdrawMoney(user, wallet, bank, amount);
                    System.out.println("Withdrawal successful!");
                } else {
                    System.out.println("Invalid choice!");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
