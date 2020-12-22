package banking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputOutputManager {

    private AccountManager accManager;
    private final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));


    public void mainCycle(String[] args) {
        String fileName = getFileName(args);
        if (fileName == null) return;
        accManager = AccountManager.getInstance();
        accManager.connectDataBase(fileName);
        int choose = 0;
        while (true) {
            System.out.println("1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit");

            String input = readInput();
            if (input != null) {
                try {
                    choose = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Print a number");
                    continue;
                }
            } else {
                break;
            }
            switch (choose) {
                case 0:
                    System.out.println("\nBye!");
                    accManager.closeConnectionDataBase();
                    System.exit(0);
                case 1:
                    String acceptance = accManager.createAccount();
                    String[] s = acceptance.split("\\\\");
                    System.out.printf("\nYour card has been created\n" +
                            "Your card number:\n%s\n" +
                            "Your card PIN:\n%s\n\n", s[0], s[1]);
                    break;
                case 2:
                    System.out.println("Enter your card number:");
                    String number = readInput();
                    System.out.println("Enter your PIN:");
                    String pin = readInput();
                    loggingAccount(number, pin);
                    break;
                default:
                    System.out.println("\nChoose right action\n");
            }
        }
    }

    private void loggingAccount(String number, String pin) {
        try {
            accManager.logIn(number, pin);
        } catch (NullPointerException npe) {
            System.out.println("\nWrong card number or PIN!\n");
            return;
        }
        System.out.println("\nYou have successfully logged in!\n");
        while (true) {
            System.out.println(
                    "1. Balance\n" +
                            "2. Add income\n" +
                            "3. Do transfer\n" +
                            "4. Close account\n" +
                            "5. Log out\n" +
                            "0. Exit");
            String input = readInput();
            int choose;
            if (input != null) {
                try {
                    choose = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Print a number");
                    continue;
                }
            } else {
                System.out.println("Choose an action");
                continue;
            }
            switch (choose) {
                case 0:
                    accManager.closeConnectionDataBase();
                    System.exit(0);
                case 1:
                    System.out.printf("\nBalance: %d\n\n", accManager.getBalance());
                    break;
                case 2:
                    addIncome();
                    break;
                case 3:
                    doTransfer();
                    break;
                case 4:
                    closeAccount();
                    System.out.println("\nThe account has been closed!\n");
                    return;
                case 5:
                    accManager.logOut();
                    System.out.println("\nYou have successfully logged out!\n");
                    return;
                default:
                    System.out.println("Choose right action");
            }
        }
    }

    private void addIncome() {
        System.out.println("\nEnter income:");
        String input = readInput();
        int income;
        if (input != null) {
            try {
                income = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Print a number");
                return;
            }
        } else {
            return;
        }
        boolean success = false;
        if (income >= 0) {
            success = accManager.addIncome(income);
        } else {
            System.out.println("Income should be a positive value!\n");
            return;
        }
        if (success) {
            System.out.println("Income was added!\n");
        } else {
            System.out.println("Error adding income!\n");
        }
    }

    private void doTransfer() {
        System.out.println("\nTransfer\n" +
                "Enter card number:");
        String cardNumber = readInput();
        if (cardNumber == null) return;
        if (!accManager.checkCardNumberWithLuhnAlgorithm(cardNumber)) {
            System.out.println("Probably you made mistake in the card number. Please try again!\n");
            return;
        }
        if (!accManager.isCardNumberExist(cardNumber)) {
            System.out.println("Such a card does not exist.\n");
            return;
        }
        if (accManager.getCard() == cardNumber) {
            System.out.println("You can't transfer money to the same account!\n");
            return;
        }
        System.out.println("Enter how much money you want to transfer:");
        String input = readInput();
        int money;
        if (input != null) {
            try {
                money = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Print a number");
                return;
            }
        } else return;
        if (money > accManager.getBalance()) {
            System.out.println("Not enough money!\n");
            return;
        }
        if (money < 0) {
            System.out.println("Print positive value\n");
            return;
        }
        accManager.transfer(cardNumber, money);
        System.out.println("Success!\n");
    }

    private void closeAccount() {
        accManager.closeAccount();
    }

    private String readInput() {
        String input = null;
        try {
            input = consoleReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    private String getFileName(String[] args) {
        String fileName = null;
        if (!args[0].toLowerCase().equals("-filename")) {
            System.out.println("Wrong command");
            return fileName;
        }
        if (!args[1].matches("\\w+\\.s3db")) {
            System.out.println("Wrong file extension");
            return fileName;
        }
        return args[1];
    }
}