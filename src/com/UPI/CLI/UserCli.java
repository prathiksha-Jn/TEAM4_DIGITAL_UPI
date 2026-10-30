package com.UPI.CLI;

import com.UPI.models.*;
import com.UPI.repository.TransactionRepo;
import com.UPI.repository.WalletRepo;
import com.UPI.services.MoneyTransferService;
import com.UPI.services.UserService;
import com.UPI.services.WalletService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class UserCli {

    private Scanner sc;
    private UserService userService;
    private WalletService walletService;
    private WalletRepo walletRepo;
    private MoneyTransferService moneyTransferService;
    private TransactionRepo transactionRepo;

    private Map<String, BankAccount> bankAccounts = new HashMap<>();

    public UserCli(Scanner sc, UserService userService) {
        this(sc, userService, new WalletService(), new WalletRepo(), null, null);
    }

    public UserCli(Scanner sc, UserService userService,
                   WalletService walletService, WalletRepo walletRepo) {
        this(sc, userService, walletService, walletRepo, null, null);
    }

    public UserCli(Scanner sc, UserService userService,
                   WalletService walletService, WalletRepo walletRepo,
                   MoneyTransferService moneyTransferService, TransactionRepo transactionRepo) {
        this.sc = sc;
        this.userService = userService;
        this.walletService = walletService;
        this.walletRepo = walletRepo;
        this.moneyTransferService = moneyTransferService;
        this.transactionRepo = transactionRepo;
    }

    public void showMenu() {

        while (true) {
            System.out.println("\n===== DIGITAL WALLET AND UPI =====");
            System.out.println("1. User Registration and Account Management");
            System.out.println("2. Wallet Operations (Add Money / Withdraw)");
            System.out.println("3. Money Transfer");
            System.out.println("4. Exit");

            System.out.print("Enter choice: ");
            if (!sc.hasNextInt()) {
                if (!sc.hasNext()) return;
                sc.next();
                System.out.println("Invalid choice!");
                continue;
            }
            int choice = sc.nextInt();
            sc.nextLine();

            try {
                switch (choice) {
                    case 1:
                        userManagementMenu();
                        break;
                    case 2:
                    case 7:
                        walletOperations();
                        break;
                    case 3:
                    case 8:
                        moneyTransferOperation();
                        break;
                    case 4:
                    case 9:
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

    // MODULE 1 - USER REGISTRATION AND ACCOUNT MANAGEMENT
    private void userManagementMenu() {
        while (true) {
            System.out.println("\n===== USER REGISTRATION AND ACCOUNT MANAGEMENT =====");
            System.out.println("1. Register User");
            System.out.println("2. View User");
            System.out.println("3. Update KYC");
            System.out.println("4. Block User");
            System.out.println("5. Unblock User");
            System.out.println("6. View All Users");
            System.out.println("7. Back");

            System.out.print("Enter choice: ");
            if (!sc.hasNextInt()) {
                if (!sc.hasNext()) return;
                sc.next();
                System.out.println("Invalid choice!");
                continue;
            }
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

        if (walletRepo != null && walletRepo.findByUserId(user.getUserId()) == null) {
            Wallet wallet = new Wallet(
                UUID.randomUUID().toString(),
                user.getUserId()
            );
            walletRepo.save(wallet);
        }

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
        if (!sc.hasNextInt()) {
            sc.nextLine();
            System.out.println("Invalid choice!");
            return;
        }
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
                UUID.randomUUID().toString(),
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
                UUID.randomUUID().toString(),
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
            if (!sc.hasNextInt()) {
                if (!sc.hasNext()) return;
                sc.next();
                System.out.println("Invalid choice!");
                continue;
            }
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
                List<Transaction> txList = transactionRepo != null ? transactionRepo.getAll() : walletService.getTransactions();
                if (txList.isEmpty()) {
                    System.out.println("No transactions found.");
                } else {
                    txList.forEach(t ->
                        System.out.println(
                            t.getTransactionType() + " | Rs."
                            + t.getAmount() + " | " + t.getStatus()
                            + (t.getFailureReason() != null ? " (" + t.getFailureReason() + ")" : "")
                        )
                    );
                }
                continue;
            }

            if (choice != 1 && choice != 2) {
                System.out.println("Invalid choice!");
                continue;
            }

            System.out.print("Enter amount: ");
            if (!sc.hasNextBigDecimal()) {
                System.out.println("Invalid amount!");
                sc.nextLine();
                continue;
            }
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

    // MODULE 3 - MONEY TRANSFER
    private void moneyTransferOperation() {
        if (moneyTransferService == null) {
            System.out.println("Error: MoneyTransferService is not initialized!");
            return;
        }

        System.out.println("\n===== MONEY TRANSFER =====");

        System.out.print("Enter sender registered phone number: ");
        String senderPhone = sc.nextLine().trim();

        User sender = userService.getUserByPhoneNumber(senderPhone);
        if (sender == null) {
            System.out.println("Error: Sender not found!");
            return;
        }

        System.out.print("Enter receiver registered phone number: ");
        String receiverPhone = sc.nextLine().trim();

        User receiver = userService.getUserByPhoneNumber(receiverPhone);
        if (receiver == null) {
            System.out.println("Error: Receiver not found!");
            return;
        }

        System.out.print("Enter transfer amount: ");
        if (!sc.hasNextBigDecimal()) {
            System.out.println("Error: Invalid amount entered!");
            sc.nextLine();
            return;
        }
        BigDecimal amount = sc.nextBigDecimal();
        sc.nextLine();

        System.out.print("Enter sender PIN: ");
        String pin = sc.nextLine().trim();

        try {
            Transaction tx = moneyTransferService.transfer(senderPhone, receiverPhone, amount, pin);

            Wallet senderWallet = walletRepo.findByUserId(sender.getUserId());
            Wallet receiverWallet = walletRepo.findByUserId(receiver.getUserId());

            if ("SUCCESS".equalsIgnoreCase(tx.getStatus())) {
                System.out.println("\n--- Transfer Successful ---");
                System.out.println("Transaction Reference ID: " + tx.getTransactionId());
                System.out.println("Status: " + tx.getStatus());
                System.out.println("Amount: Rs." + tx.getAmount());
                System.out.println("Sender (" + sender.getName() + ") New Wallet Balance: Rs."
                        + (senderWallet != null ? senderWallet.getBalance() : "N/A"));
                System.out.println("Receiver (" + receiver.getName() + ") New Wallet Balance: Rs."
                        + (receiverWallet != null ? receiverWallet.getBalance() : "N/A"));
            } else {
                System.out.println("\n--- Transfer Failed ---");
                System.out.println("Transaction Reference ID: " + tx.getTransactionId());
                System.out.println("Status: " + tx.getStatus());
                System.out.println("Failure Reason: " + tx.getFailureReason());
                System.out.println("Sender (" + sender.getName() + ") Wallet Balance: Rs."
                        + (senderWallet != null ? senderWallet.getBalance() : "N/A"));
                System.out.println("Receiver (" + receiver.getName() + ") Wallet Balance: Rs."
                        + (receiverWallet != null ? receiverWallet.getBalance() : "N/A"));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Transfer Failed: " + e.getMessage());
        }
    }
}
