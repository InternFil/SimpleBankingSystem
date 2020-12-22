package banking;

import java.util.Arrays;
import java.util.Random;

public class Account {

    private String cardNumber;
    private String PIN;
    private int balance = 0;

    Account () {
        cardNumber = generateCardNumberWithLuhnAlgorithm();
        PIN = generatePIN();
    }

    Account (String cardNumber, String PIN, int balance) {
        this.cardNumber = cardNumber;
        this.PIN = PIN;
        this.balance = balance;
    }

    private int generateBalance() {
        Random random = new Random();
        int balance = random.nextInt(101);
        return balance;
    }

    private String generateCardNumberWithLuhnAlgorithm() {
        Random randomizerCardNumber = new Random();
        StringBuilder assembler = new StringBuilder();
        String BIN = "400000";
        int checksum = 0;
        for (int i = 0; i < 9; i++) {
            int accountIdentifierElem = randomizerCardNumber.nextInt(10);
            assembler.append(accountIdentifierElem);
        }
        String s = BIN + assembler.toString();
        char[] charArray = s.toCharArray();
        int[] fifteenDigits = new int[15];
        int sum = 0;
        for (int i = 0; i < charArray.length; i++) {
            fifteenDigits[i] = Character.getNumericValue(charArray[i]);
        }
        for (int i = 0; i < fifteenDigits.length; i++) {
            if (i == 0 || i % 2 == 0) {
                int num = 2 * fifteenDigits[i];
                if (num > 9) {
                    num -= 9;
                }
                sum += num;
            } else {
                sum += fifteenDigits[i];
            }
        }
        if (sum % 10 == 0) {
            checksum = 0;
        } else {
            checksum = 10 - sum % 10;
        }
        return s + checksum;
    }

    private String generatePIN() {
        Random randomizerPIN = new Random();
        StringBuilder assembler = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int val = randomizerPIN.nextInt(10);
            assembler.append(val);
        }
        return assembler.toString();
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPIN() {
        return PIN;
    }

    public int getBalance() {
        return balance;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}