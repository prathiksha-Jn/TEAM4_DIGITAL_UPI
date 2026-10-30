package com.UPI.CLI;

import com.UPI.models.*;
import com.UPI.repository.TransactionRepo;
import com.UPI.repository.UserRepo;
import com.UPI.repository.WalletRepo;
import com.UPI.services.MoneyTransferService;
import com.UPI.services.UserService;
import com.UPI.services.WalletService;

import java.math.BigDecimal;
import java.util.List;

public class IntegrationTestSuite {

    private static int passedTests = 0;
    private static int failedTests = 0;

    private static void assertTrue(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("[PASS] " + testName + " - " + details);
            passedTests++;
        } else {
            System.err.println("[FAIL] " + testName + " - " + details);
            failedTests++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("RUNNING FULL E2E INTEGRATION TEST SUITE");
        System.out.println("=================================================");

        // Shared architecture instances
        UserRepo userRepo = new UserRepo();
        WalletRepo walletRepo = new WalletRepo();
        TransactionRepo transactionRepo = new TransactionRepo();

        UserService userService = new UserService(userRepo);
        WalletService walletService = new WalletService(transactionRepo);
        MoneyTransferService transferService =
                new MoneyTransferService(userRepo, walletRepo, transactionRepo);

        // -------------------------------------------------------------------
        // Scenario 1: Register two users dynamically.
        // -------------------------------------------------------------------
        User user1 = userService.registerUser("Alice Smith", "9876543201", "alice@example.com", "1111");
        Wallet wallet1 = new Wallet(java.util.UUID.randomUUID().toString(), user1.getUserId());
        walletRepo.save(wallet1);

        User user2 = userService.registerUser("Bob Jones", "9876543202", "bob@example.com", "2222");
        Wallet wallet2 = new Wallet(java.util.UUID.randomUUID().toString(), user2.getUserId());
        walletRepo.save(wallet2);

        assertTrue("Scenario 1",
                userRepo.findByPhoneNumber("9876543201") != null &&
                userRepo.findByPhoneNumber("9876543202") != null &&
                walletRepo.findByUserId(user1.getUserId()) != null &&
                walletRepo.findByUserId(user2.getUserId()) != null,
                "Both users registered dynamically and wallets stored in shared WalletRepo");

        // -------------------------------------------------------------------
        // Scenario 2: Link a bank account and add money to the first user's wallet.
        // -------------------------------------------------------------------
        BankAccount aliceBank = new BankAccount(
                java.util.UUID.randomUUID().toString(),
                user1.getUserId(),
                "ACC1001",
                "HDFC Bank",
                new BigDecimal("20000"),
                true
        );

        walletService.addMoney(user1, wallet1, aliceBank, new BigDecimal("8000"));

        assertTrue("Scenario 2",
                wallet1.getBalance().compareTo(new BigDecimal("8000")) == 0 &&
                aliceBank.getBalance().compareTo(new BigDecimal("12000")) == 0 &&
                transactionRepo.getAll().stream().anyMatch(t -> "ADD_MONEY".equals(t.getTransactionType()) && t.getAmount().compareTo(new BigDecimal("8000")) == 0),
                "Bank linked and Rs.8,000 added to Alice's wallet; balance updated in shared WalletRepo and transaction recorded in shared TransactionRepo");

        // -------------------------------------------------------------------
        // Scenario 3 & 4: Transfer money from first user to second user & verify balances.
        // -------------------------------------------------------------------
        Transaction tx1 = transferService.transfer("9876543201", "9876543202", new BigDecimal("2500"), "1111");

        assertTrue("Scenario 3 & 4",
                "SUCCESS".equals(tx1.getStatus()) &&
                wallet1.getBalance().compareTo(new BigDecimal("5500")) == 0 &&
                wallet2.getBalance().compareTo(new BigDecimal("2500")) == 0,
                "Transferred Rs.2,500 from Alice to Bob. Alice balance: Rs.5,500, Bob balance: Rs.2,500");

        // -------------------------------------------------------------------
        // Scenario 5: Verify the transaction appears in the shared TransactionRepo.
        // -------------------------------------------------------------------
        boolean foundInRepo = transactionRepo.getAll().stream().anyMatch(t ->
                "TRANSFER".equals(t.getTransactionType()) &&
                t.getAmount().compareTo(new BigDecimal("2500")) == 0 &&
                "SUCCESS".equals(t.getStatus()) &&
                t.getTransactionId().equals(tx1.getTransactionId())
        );

        assertTrue("Scenario 5", foundInRepo,
                "Transfer transaction found in shared TransactionRepo with ID: " + tx1.getTransactionId());

        // -------------------------------------------------------------------
        // Scenario 6: Try transferring to an unregistered phone number.
        // -------------------------------------------------------------------
        boolean unregCaught = false;
        try {
            transferService.transfer("9876543201", "9999999999", new BigDecimal("500"), "1111");
        } catch (IllegalArgumentException e) {
            unregCaught = e.getMessage().contains("Receiver not found");
        }

        assertTrue("Scenario 6", unregCaught &&
                wallet1.getBalance().compareTo(new BigDecimal("5500")) == 0,
                "Transfer to unregistered number rejected with error message and balances unchanged");

        // -------------------------------------------------------------------
        // Scenario 7: Try transferring with an incorrect PIN.
        // -------------------------------------------------------------------
        boolean wrongPinCaught = false;
        try {
            transferService.transfer("9876543201", "9876543202", new BigDecimal("500"), "9999");
        } catch (IllegalArgumentException e) {
            wrongPinCaught = e.getMessage().contains("Incorrect PIN");
        }

        assertTrue("Scenario 7", wrongPinCaught &&
                wallet1.getBalance().compareTo(new BigDecimal("5500")) == 0 &&
                wallet2.getBalance().compareTo(new BigDecimal("2500")) == 0,
                "Transfer with incorrect PIN rejected; remaining attempts displayed; balances unchanged");

        // Reset Alice's wrong pin attempts from test 7 for subsequent tests
        transferService.resetPinAttempts("9876543201");

        // -------------------------------------------------------------------
        // Scenario 8: Test insufficient wallet balance.
        // -------------------------------------------------------------------
        // Bob has Rs.2,500. He tries to transfer Rs.4,000 to Alice.
        Transaction failedTx = transferService.transfer("9876543202", "9876543201", new BigDecimal("4000"), "2222");

        assertTrue("Scenario 8",
                "FAILED".equals(failedTx.getStatus()) &&
                "Insufficient wallet balance".equals(failedTx.getFailureReason()) &&
                wallet2.getBalance().compareTo(new BigDecimal("2500")) == 0 &&
                wallet1.getBalance().compareTo(new BigDecimal("5500")) == 0 &&
                transactionRepo.getAll().stream().anyMatch(t -> t.getTransactionId().equals(failedTx.getTransactionId())),
                "Insufficient balance transfer returned FAILED status with failure reason; both balances unchanged; recorded in shared TransactionRepo");

        // -------------------------------------------------------------------
        // Scenario 9: Test transfer amount limits (min < 1, max > 10,000).
        // -------------------------------------------------------------------
        boolean minCaught = false;
        try {
            transferService.transfer("9876543201", "9876543202", new BigDecimal("0.50"), "1111");
        } catch (IllegalArgumentException e) {
            minCaught = e.getMessage().contains("between Rs.1 and Rs.10,000");
        }

        boolean maxCaught = false;
        try {
            transferService.transfer("9876543201", "9876543202", new BigDecimal("10001"), "1111");
        } catch (IllegalArgumentException e) {
            maxCaught = e.getMessage().contains("between Rs.1 and Rs.10,000");
        }

        assertTrue("Scenario 9", minCaught && maxCaught &&
                wallet1.getBalance().compareTo(new BigDecimal("5500")) == 0,
                "Transfer amount validation enforced min (Rs.1) and max (Rs.10,000)");

        // -------------------------------------------------------------------
        // Scenario 10: Test daily transfer limit (Rs.25,000 per sender).
        // -------------------------------------------------------------------
        // Alice already transferred Rs.2,500 today.
        // Upgrade Bob to FULL KYC so receiver wallet limit (Rs.100,000) does not block daily limit test.
        user2.setKycStatus(KycStatus.FULL);
        user1.setKycStatus(KycStatus.FULL); // so monthly limit doesn't interfere with daily limit test
        wallet1.setBalance(new BigDecimal("30000")); // top up Alice for testing daily limit

        // Transfer 1: Rs.10,000 (total today: 12,500)
        transferService.transfer("9876543201", "9876543202", new BigDecimal("10000"), "1111");

        // Transfer 2: Rs.10,000 (total today: 22,500)
        transferService.transfer("9876543201", "9876543202", new BigDecimal("10000"), "1111");

        // Transfer 3: Rs.3,000 (would bring total to 25,500 > 25,000)
        boolean dailyLimitCaught = false;
        BigDecimal balBeforeDailyLimit = wallet1.getBalance();
        try {
            transferService.transfer("9876543201", "9876543202", new BigDecimal("3000"), "1111");
        } catch (IllegalArgumentException e) {
            dailyLimitCaught = e.getMessage().contains("Daily transfer limit");
        }

        assertTrue("Scenario 10", dailyLimitCaught &&
                wallet1.getBalance().compareTo(balBeforeDailyLimit) == 0,
                "Daily transfer limit of Rs.25,000 strictly enforced; 3rd transfer rejected; balances unchanged");

        // -------------------------------------------------------------------
        // Scenario 11: Test three incorrect PIN attempts and the 24-hour lock.
        // -------------------------------------------------------------------
        User userLockTest = userService.registerUser("Charlie Brown", "9876543203", "charlie@example.com", "4321");
        Wallet walletCharlie = new Wallet(java.util.UUID.randomUUID().toString(), userLockTest.getUserId());
        walletCharlie.setBalance(new BigDecimal("5000"));
        walletRepo.save(walletCharlie);

        boolean attempt1 = false;
        try {
            transferService.transfer("9876543203", "9876543202", new BigDecimal("100"), "0000");
        } catch (IllegalArgumentException e) {
            attempt1 = e.getMessage().contains("Attempts remaining: 2");
        }

        boolean attempt2 = false;
        try {
            transferService.transfer("9876543203", "9876543202", new BigDecimal("100"), "0000");
        } catch (IllegalArgumentException e) {
            attempt2 = e.getMessage().contains("Attempts remaining: 1");
        }

        boolean attempt3Locked = false;
        try {
            transferService.transfer("9876543203", "9876543202", new BigDecimal("100"), "0000");
        } catch (IllegalArgumentException e) {
            attempt3Locked = e.getMessage().contains("Transactions locked for 24 hours");
        }

        boolean attempt4StillLockedEvenWithCorrectPin = false;
        try {
            transferService.transfer("9876543203", "9876543202", new BigDecimal("100"), "4321");
        } catch (IllegalArgumentException e) {
            attempt4StillLockedEvenWithCorrectPin = e.getMessage().contains("Transactions locked for 24 hours");
        }

        assertTrue("Scenario 11",
                attempt1 && attempt2 && attempt3Locked && attempt4StillLockedEvenWithCorrectPin &&
                transferService.isLocked("9876543203"),
                "3 incorrect PIN attempts locked transactions for 24 hours; correct PIN also rejected while locked");

        // -------------------------------------------------------------------
        // Scenario 12: Verify Module 1 registration and Module 2 Add Money/Withdraw still work.
        // -------------------------------------------------------------------
        User userMod = userService.registerUser("David Miller", "9876543204", "david@example.com", "5555");
        Wallet walletDavid = new Wallet(java.util.UUID.randomUUID().toString(), userMod.getUserId());
        walletRepo.save(walletDavid);

        BankAccount davidBank = new BankAccount(
                java.util.UUID.randomUUID().toString(),
                userMod.getUserId(),
                "ACC4001",
                "SBI",
                new BigDecimal("15000"),
                true
        );

        // Add money (Module 2)
        Transaction modAdd = walletService.addMoney(userMod, walletDavid, davidBank, new BigDecimal("3000"));

        // Withdraw money (Module 2)
        Transaction modWithdraw = walletService.withdrawMoney(userMod, walletDavid, davidBank, new BigDecimal("1000"));

        // KYC check (Module 1)
        userService.updateKycStatus("9876543204", KycStatus.FULL);

        assertTrue("Scenario 12",
                "ADD_MONEY".equals(modAdd.getTransactionType()) &&
                "WITHDRAW".equals(modWithdraw.getTransactionType()) &&
                walletDavid.getBalance().compareTo(new BigDecimal("2000")) == 0 &&
                davidBank.getBalance().compareTo(new BigDecimal("13000")) == 0 &&
                userMod.getKycStatus() == KycStatus.FULL &&
                transactionRepo.getAll().contains(modAdd) &&
                transactionRepo.getAll().contains(modWithdraw),
                "Module 1 registration/KYC and Module 2 Add Money/Withdraw operations work seamlessly with shared state");

        // -------------------------------------------------------------------
        // Scenario Bonus: Basic KYC Monthly Limit check
        // -------------------------------------------------------------------
        User userKycTest = userService.registerUser("Eva Green", "9876543205", "eva@example.com", "7777");
        Wallet walletEva = new Wallet(java.util.UUID.randomUUID().toString(), userKycTest.getUserId());
        walletEva.setBalance(new BigDecimal("25000"));
        walletRepo.save(walletEva);
        // Eva has BASIC KYC -> monthly limit is Rs.20,000

        // Transfer 1: Rs.10,000
        transferService.transfer("9876543205", "9876543202", new BigDecimal("10000"), "7777");
        // Transfer 2: Rs.10,000
        transferService.transfer("9876543205", "9876543202", new BigDecimal("10000"), "7777");

        // Transfer 3: Rs.1,000 -> Should exceed Rs.20,000 monthly limit
        boolean monthlyLimitCaught = false;
        try {
            transferService.transfer("9876543205", "9876543202", new BigDecimal("1000"), "7777");
        } catch (IllegalArgumentException e) {
            monthlyLimitCaught = e.getMessage().contains("Monthly transfer limit of Rs.20000 exceeded");
        }

        assertTrue("Bonus Validation", monthlyLimitCaught,
                "Basic KYC monthly transfer limit of Rs.20,000 strictly enforced");

        System.out.println("=================================================");
        System.out.println("TEST SUITE RESULTS: " + passedTests + " PASSED, " + failedTests + " FAILED");
        System.out.println("=================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }
}
