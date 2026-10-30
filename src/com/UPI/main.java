package com.UPI;

import com.UPI.CLI.UserCli;
import com.UPI.repository.UserRepo;
import com.UPI.services.UserService;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        UserRepo userRepo = new UserRepo();
        UserService userService = new UserService(userRepo);

        UserCli userCli = new UserCli(sc, userService);

        System.out.println("===== DIGITAL WALLET AND UPI =====");

        userCli.showMenu();

        sc.close();
    }
}
