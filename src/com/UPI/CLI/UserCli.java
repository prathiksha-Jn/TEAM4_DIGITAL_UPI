
package com.UPI.CLI;

import com.UPI.models.*;
import com.UPI.repository.WalletRepo;
import com.UPI.services.UserService;
import com.UPI.services.WalletService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UserCli {

    private Scanner sc;
    private UserService userService;
    private WalletService walletService;
    private WalletRepo walletRepo;

    private Map<String, BankAccount> bankAccounts = new HashMap<>();

    public UserCli(Scanner sc, UserService userService) {
        this.sc = sc;
        this.userService = userService;
        this.walletService = new WalletService();
        this.walletRepo = new WalletRepo();
    }

    public void showMenu() {

        while (true) {
            System.out.println("\n===== DIGITAL WALLET =====");
            System.out.println("1. Register User");
            System.out.println("2. View User");
            System.out.println("3. Update KYC");
            System.out.println("4. Block User");
            System.out.println("5. Unblock User");
            System.out.println("6. View All Users");
            System.out.println("7. Wallet Operations");
            System.out.println("8. Exit");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            try {
                switch (choice) {
                    case 1:
                        registerUser();
                        break;
                    case 2:
                        viewUser();
                        break;
                    case 3:
                        updateKyc();
                        break;
                    case 4:
                        blockUser();
                        break;
                    case 5:
                        unblockUser();
                        break;
                    case 6:
                        userService.displayAllUsers();
                        break;
                    case 7:
                        walletOperations();
                        break;
                    case 8:
                        System.out.println("Thank you!");
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void registerUser() {

        System.out.print("Enter name: ");
        String name = sc.nextLine();

        System.out.print("Enter phone: ");
        String phone = sc.nextLine();

        System.out.print("Enter email: ");
        String email = sc.nextLine();

        System.out.print("Enter PIN: ");
        String pin = sc.nextLine();

        User user = userService.registerUser(
            name, phone, email, pin
        );

        System.out.println("Registration successful!");
        System.out.println("User ID: " + user.getUserId());
    }

    private void viewUser() {

        System.out.print("Enter phone: ");
        User user = userService.getUserByPhoneNumber(sc.nextLine());

        if (user == null) {
            System.out.println("User not found!");
            return;
        }

        System.out.println("Name: " + user.getName());
        System.out.println("Phone: " + user.getPhoneNumber());
        System.out.println("KYC: " + user.getKycStatus());
        System.out.println("Status: " + user.getUserStatus());
        System.out.println("Wallet Limit: " + user.getWalletLimit());
    }

    private void updateKyc() {

        System.out.print("Enter phone: ");
        String phone = sc.nextLine();

        System.out.println("1. BASIC");
        System.out.println("2. FULL");
        int choice = sc.nextInt();
        sc.nextLine();

        KycStatus status;

        if (choice == 1)
            status = KycStatus.BASIC;
        else if (choice == 2)
            status = KycStatus.FULL;
        else {
            System.out.println("Invalid choice!");
            return;
        }

        userService.updateKycStatus(phone, status);
        System.out.println("KYC updated!");
    }

    private void blockUser() {
        System.out.print("Enter phone: ");
        userService.blockUser(sc.nextLine());
        System.out.println("User blocked!");
    }

    private void unblockUser() {
        System.out.print("Enter phone: ");
        userService.unblockUser(sc.nextLine());
        System.out.println("User unblocked!");
    }

    // MODULE 2 - WALLET OPERATIONS
    private void walletOperations() {

        System.out.print("Enter registered phone number: ");
        String phone = sc.nextLine();

        User user = userService.getUserByPhoneNumber(phone);

        if (user == null) {
            System.out.println("User not found!");
            return;
        }

        Wallet wallet = walletRepo.findByUserId(user.getUserId());

        if (wallet == null) {
            wallet = new Wallet(
                java.util.UUID.randomUUID().toString(),
                user.getUserId()
            );
            walletRepo.save(wallet);
        }

        BankAccount bank = bankAccounts.get(user.getUserId());

        if (bank == null) {
            System.out.print("Enter bank name: ");
            String bankName = sc.nextLine();

            System.out.print("Enter account number: ");
            String accountNumber = sc.nextLine();

            System.out.print("Enter initial bank balance: ");
            BigDecimal balance = sc.nextBigDecimal();
            sc.nextLine();

            bank = new BankAccount(
                java.util.UUID.randomUUID().toString(),
                user.getUserId(),
                accountNumber,
                bankName,
                balance,
                true
            );

            bankAccounts.put(user.getUserId(), bank);
        }

        while (true) {

            System.out.println("\n===== WALLET OPERATIONS =====");
            System.out.println("1. Add Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Wallet Balance");
            System.out.println("4. Bank Balance");
            System.out.println("5. Transaction History");
            System.out.println("6. Back");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 6)
                return;

            if (choice == 3) {
                System.out.println("Wallet Balance: Rs."
                    + wallet.getBalance());
                continue;
            }

            if (choice == 4) {
                System.out.println("Bank Balance: Rs."
                    + bank.getBalance());
                continue;
            }

            if (choice == 5) {
                walletService.getTransactions().forEach(t ->
                    System.out.println(
                        t.getTransactionType() + " | Rs."
                        + t.getAmount() + " | " + t.getStatus()
                    )
                );
                continue;
            }

            if (choice != 1 && choice != 2) {
                System.out.println("Invalid choice!");
                continue;
            }

            System.out.print("Enter amount: ");
            BigDecimal amount = sc.nextBigDecimal();
            sc.nextLine();

            try {
                if (choice == 1) {
                    walletService.addMoney(user, wallet, bank, amount);
                    System.out.println("Money added successfully!");
                } else {
                    walletService.withdrawMoney(user, wallet, bank, amount);
                    System.out.println("Money withdrawn successfully!");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Transaction failed: " + e.getMessage());
            }
        }
    }
}
