
package com.UPI.repository;

import java.util.HashMap;
import java.util.Map;

import com.UPI.models.User;

public class UserRepo {

    private Map<String, User> users = new HashMap<>();

    // Save a new user
    public void saveUser(User user) {
        users.put(user.getPhoneNumber(), user);
    }

    // Find user using phone number
    public User findByPhoneNumber(String phoneNumber) {
        return users.get(phoneNumber);
    }

    // Check whether phone number already exists
    public boolean existsByPhoneNumber(String phoneNumber) {
        return users.containsKey(phoneNumber);
    }

    // Find user using user ID
    public User findByUserId(String userId) {

        for (User user : users.values()) {

            if (user.getUserId().equals(userId)) {
                return user;
            }
        }

        return null;
    }

    // Get all users
    public Map<String, User> getAllUsers() {
        return users;
    }
}
