
package com.UPI.services;

import com.UPI.models.User;
import com.UPI.models.KycStatus;
import com.UPI.models.UserStatus;
import com.UPI.repository.UserRepo;

public class UserService {

    private UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    // Register a new user
    public User registerUser(String name, String phoneNumber,
                             String email, String pin) {

        // Name validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        // Phone number validation
        if (phoneNumber == null || !phoneNumber.matches("[0-9]{10}")) {
            throw new IllegalArgumentException("Phone number must contain 10 digits");
        }

        // Email validation
        if (email == null ||
            !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        // PIN validation
        if (pin == null || !pin.matches("[0-9]{4}")) {
            throw new IllegalArgumentException("PIN must contain exactly 4 digits");
        }

        // Duplicate phone number check
        if (userRepo.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        User user = new User(name, phoneNumber, email, pin);

        userRepo.saveUser(user);

        return user;
    }

    // Find user by phone number
    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepo.findByPhoneNumber(phoneNumber);
    }

    // Find user by ID
    public User getUserById(String userId) {
        return userRepo.findByUserId(userId);
    }

    // Update KYC status
    public void updateKycStatus(String phoneNumber, KycStatus kycStatus) {

        User user = userRepo.findByPhoneNumber(phoneNumber);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setKycStatus(kycStatus);
    }

    // Block user
    public void blockUser(String phoneNumber) {

        User user = userRepo.findByPhoneNumber(phoneNumber);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setUserStatus(UserStatus.BLOCKED);
    }

    // Unblock user
    public void unblockUser(String phoneNumber) {

        User user = userRepo.findByPhoneNumber(phoneNumber);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setUserStatus(UserStatus.ACTIVE);
    }

    // Get all users
    public void displayAllUsers() {

        for (User user : userRepo.getAllUsers().values()) {

            System.out.println("User ID: " + user.getUserId());
            System.out.println("Name: " + user.getName());
            System.out.println("Phone: " + user.getPhoneNumber());
            System.out.println("Email: " + user.getEmail());
            System.out.println("KYC: " + user.getKycStatus());
            System.out.println("Status: " + user.getUserStatus());
            System.out.println("-------------------------");
        }
    }
}
