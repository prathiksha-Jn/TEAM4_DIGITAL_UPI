
package com.UPI;

import java.util.Scanner;

import com.UPI.repository.UserRepo;
import com.UPI.services.UserService;
import com.UPI.CLI.UserCli;

public class main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        UserRepo userRepo = new UserRepo();

        UserService userService = new UserService(userRepo);

        UserCli userCli = new UserCli(sc, userService);

        userCli.showMenu();

        sc.close();
    }
}
