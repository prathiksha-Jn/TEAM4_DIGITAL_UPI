
package com.UPI.CLI;

import java.util.Scanner;

import com.UPI.models.User;
import com.UPI.models.KycStatus;
import com.UPI.services.UserService;

public class UserCli {

    private Scanner sc;
    private UserService userService;

    public UserCli(Scanner sc, UserService userService) {
        this.sc = sc;
        this.userService = userService;
    }

    public void showMenu() {

        while (true) {

            System.out.println("\n===== USER MANAGEMENT =====");
            System.out.println("1. Register User");
            System.out.println("2. View User");
            System.out.println("3. Update KYC");
            System.out.println("4. Block User");
            System.out.println("5. Unblock User");
            System.out.println("6. View All Users");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");

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
                        System.out.println("Exiting User Management...");
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

        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();

        System.out.print("Enter email: ");
        String email = sc.nextLine();

        System.out.print("Enter 4-digit PIN: ");
        String pin = sc.nextLine();

        User user = userService.registerUser(name, phone, email, pin);

        System.out.println("User registered successfully!");
        System.out.println("User ID: " + user.getUserId());
    }

    private void viewUser() {

        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();

        User user = userService.getUserByPhoneNumber(phone);

        if (user == null) {
            System.out.println("User not found!");
            return;
        }

        System.out.println("\n----- USER DETAILS -----");
        System.out.println("User ID: " + user.getUserId());
        System.out.println("Name: " + user.getName());
        System.out.println("Phone: " + user.getPhoneNumber());
        System.out.println("Email: " + user.getEmail());
        System.out.println("KYC: " + user.getKycStatus());
        System.out.println("Status: " + user.getUserStatus());
        System.out.println("Wallet Limit: " + user.getWalletLimit());
        System.out.println("Monthly Transfer Limit: " +
                user.getMonthlyTransferLimit());
    }

    private void updateKyc() {

        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();

        System.out.println("1. BASIC");
        System.out.println("2. FULL");
        System.out.print("Select KYC: ");

        int choice = sc.nextInt();
        sc.nextLine();

        KycStatus status;

        if (choice == 1) {
            status = KycStatus.BASIC;
        } else if (choice == 2) {
            status = KycStatus.FULL;
        } else {
            System.out.println("Invalid KYC choice!");
            return;
        }

        userService.updateKycStatus(phone, status);

        System.out.println("KYC updated successfully!");
    }

    private void blockUser() {

        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();

        userService.blockUser(phone);

        System.out.println("User blocked successfully!");
    }

    private void unblockUser() {

        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();

        userService.unblockUser(phone);

        System.out.println("User unblocked successfully!");
    }
}
