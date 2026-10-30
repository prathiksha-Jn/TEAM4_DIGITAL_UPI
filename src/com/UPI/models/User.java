
package com.UPI.models;

import java.util.UUID;

public class User {

    private String userId;
    private String name;
    private String phoneNumber;
    private String email;
    private String pin;

    private KycStatus kycStatus;
    private UserStatus userStatus;

    public User(String name, String phoneNumber, String email, String pin) {

        this.userId = UUID.randomUUID().toString();
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.pin = pin;

        this.kycStatus = KycStatus.BASIC;
        this.userStatus = UserStatus.ACTIVE;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public boolean verifyPin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public double getWalletLimit() {
        return kycStatus.getWalletLimit();
    }

    public Double getMonthlyTransferLimit() {
        return kycStatus.getMonthlyTransferLimit();
    }
}
