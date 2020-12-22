package banking;

public class AccountManager {

    private static AccountManager am;
    private Account current;
    private DataBaseManager dbm;

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        if (am == null) {
            am = new AccountManager();
        }
        return am;
    }

    public void connectDataBase(String fileName) {
        dbm = new DataBaseManager();
        dbm.initialize(fileName);
    }

    private void recordCard() {
        dbm.insertAccount(current.getCardNumber(), current.getPIN());
    }

    public void logIn(String cardNumber, String pin) {
        current = null;
        current = dbm.getAccount(cardNumber, pin);
        if (current == null || !checkCardNumberWithLuhnAlgorithm(cardNumber)) {
            throw new NullPointerException();
        }
    }

    public boolean addIncome(int income) {
        boolean success = false;
        if (dbm.addIncome(current.getCardNumber(), income)) {
            current.setBalance(current.getBalance() + income);
            success = true;
        } else {
            success = false;
        }
        return success;
    }

    public boolean subtractMoney(int money) {
        boolean success = false;
        if (dbm.subtractIncome(current.getCardNumber(), money)) {
            current.setBalance(current.getBalance() - money);
            success = true;
        } else {
            success = false;
        }
        return success;
    }

    public boolean isCardNumberExist(String cardNumber) {
        return dbm.checkCardNumber(cardNumber);
    }

    public void transfer(String cardNumber, int money) {
        dbm.transferMoney(cardNumber, money);
        subtractMoney(money);
    }

    public void closeAccount() {
        dbm.closeAccount(current.getCardNumber());
    }

    public int getBalance() {
        return current.getBalance();
    }

    public String getCard() {
        return current.getCardNumber();
    }

    public String getPIN() {
        return current.getPIN();
    }

    public void closeConnectionDataBase() {
        dbm.closeDataBaseConnection();
    }

    public void logOut() {
        current = null;
    }

    public boolean checkCardNumberWithLuhnAlgorithm(String card) {
        if (card.length() != 16) {
            return false;
        }
        char[] charArray = card.toCharArray();
        int[] fifteenDigits = new int[15];
        int sum = 0;
        int checksum = Character.getNumericValue(charArray[15]);
        for (int i = 0; i < 15; i++) {
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
        return (sum + checksum) % 10 == 0;
    }

    String createAccount() {
        current = new Account();
        String response = String.format("%s\\%s", current.getCardNumber(), current.getPIN());
        recordCard();
        return response;
    }
}
