package banking;

import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;


public class DataBaseManager {

    private SQLiteDataSource dataSource;
    private Connection connection;

    public void initialize(String fileName) {
        Path path = Paths.get(fileName);
        if (isDataBaseFileExists(path)) {
            openDataBaseConnection(fileName);
        } else {
            createDataBaseFile(path);
            openDataBaseConnection(fileName);
            createCardTable();
        }
    }

    public void createCardTable() {
        String createCardTable = "CREATE TABLE IF NOT EXISTS card " +
                "(id INTEGER PRIMARY KEY," +
                "number TEXT NOT NULL," +
                "pin TEXT NOT NULL," +
                "balance INTEGER DEFAULT 0);";
        try (PreparedStatement pstmt = connection.prepareStatement(createCardTable)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertAccount(String cardNumber, String pin) {
        try {
            if (connection.isValid(0)) {
                String insertCard = "INSERT INTO card (number, pin) VALUES (? , ?);";
                try (PreparedStatement pstmt = connection.prepareStatement(insertCard)) {
                    pstmt.setString(1, cardNumber);
                    pstmt.setString(2, pin);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Account getAccount(String cardNumber, String pin) {
        ResultSet resultSet = null;
        Account account = null;
        try {
            if (connection.isValid(0)) {
                String select = "SELECT number, pin, balance FROM card GROUP BY id HAVING number = ? AND pin = ?;";
                try (PreparedStatement pstmt = connection.prepareStatement(select)) {
                    pstmt.setString(1, cardNumber);
                    pstmt.setString(2, pin);
                    resultSet = pstmt.executeQuery();
                    if (!resultSet.next()) return account;
                    String number = resultSet.getString("number");
                    String PIN = resultSet.getString("pin");
                    int balance = resultSet.getInt("balance");
                    account = new Account(number, PIN, balance);
                } catch (SQLException ex) {
                    System.err.println("Error receiving data from database");
                    ex.printStackTrace();
                }
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return account;
    }

    public boolean addIncome(String cardNumber, int income) {
        boolean success = false;
        String update = "UPDATE card SET balance = balance + ? WHERE number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(update)) {
            connection.setAutoCommit(false);
            pstmt.setInt(1, income);
            pstmt.setString(2, cardNumber);
            pstmt.executeUpdate();
            connection.commit();
            success = true;
        } catch (SQLException throwables) {
            System.err.println("Error adding money");
            throwables.printStackTrace();
            success = false;
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return success;
    }

    public boolean subtractIncome(String cardNumber, int money) {
        boolean success = false;
        String update = "UPDATE card SET balance = balance - ? WHERE number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(update)) {
            connection.setAutoCommit(false);
            pstmt.setInt(1, money);
            pstmt.setString(2, cardNumber);
            pstmt.executeUpdate();
            connection.commit();
            success = true;
        } catch (SQLException throwables) {
            System.err.println("Error subtract money");
            throwables.printStackTrace();
            success = false;
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return success;
    }

    public boolean checkCardNumber(String cardNumber) {
        ResultSet resultSet = null;
        boolean isCardExist = false;
        String query = "SELECT EXISTS (SELECT number FROM card WHERE number = ?);";
        //String query = "SELECT number FROM card GROUP BY id HAVING number = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, cardNumber);
            resultSet = pstmt.executeQuery();
            isCardExist = resultSet.getBoolean(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return isCardExist;
    }

    public boolean transferMoney(String cardNumber, int money) {
        boolean success = false;
        String update = "UPDATE card SET balance = ? WHERE number = ?;";
        try (PreparedStatement statement = connection.prepareStatement(update)) {
            connection.setAutoCommit(false);
            statement.setInt(1, money);
            statement.setString(2, cardNumber);
            statement.executeUpdate();
            connection.commit();
            success = true;
        } catch (SQLException throwables) {
            System.err.println("Error transferring money");
            throwables.printStackTrace();
            success = false;
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return success;
    }

    public void closeAccount(String cardNumber) {
        String delete = "DELETE FROM card WHERE ?;";
        try(PreparedStatement pstmt = connection.prepareStatement(delete)) {
            pstmt.setString(1, cardNumber);
            pstmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void createDataBaseFile(Path path) {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            System.err.println("Can't create Data Base file");
            e.printStackTrace();
        }
    }

    private boolean isDataBaseFileExists(Path path) {
        return Files.exists(path);
    }

    public void openDataBaseConnection(String fileName) {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + fileName);
        try {
            connection = dataSource.getConnection();
        } catch (SQLException throwables) {
            System.err.println("Can't connect to Data Base");
            throwables.printStackTrace();
        }
    }

    public void closeDataBaseConnection() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
