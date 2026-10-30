
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

    String name;
    String phone;
    String email;
    String pin;

    // Name validation
    while (true) {
        System.out.print("Enter name: ");
        name = sc.nextLine().trim();

        if (name.matches("[a-zA-Z ]+")) {
            break;
        }

        System.out.println("Invalid name! Enter letters only.");
    }

    // Phone validation
    while (true) {
        System.out.print("Enter phone number: ");
        phone = sc.nextLine().trim();

        if (!phone.matches("[0-9]{10}")) {
            System.out.println("Invalid phone number! Enter exactly 10 digits.");
        } 
        else if (userService.getUserByPhoneNumber(phone) != null) {
            System.out.println("Phone number already registered! Enter another number.");
        } 
        else {
            break;
        }
    }

    // Email validation
    while (true) {
        System.out.print("Enter email: ");
        email = sc.nextLine().trim();

        if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            break;
        }

        System.out.println("Invalid email! Enter a valid email address.");
    }

    // PIN validation
    while (true) {
        System.out.print("Enter 4-digit PIN: ");
        pin = sc.nextLine().trim();

        if (pin.matches("[0-9]{4}")) {
            break;
        }

        System.out.println("Invalid PIN! Enter exactly 4 digits.");
    }

    User user = userService.registerUser(name, phone, email, pin);

    System.out.println("\nUser registered successfully!");
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
