import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Scanner;

public class DB {
    private static String url = "jdbc:mysql://localhost:3306/";
    private static String username = "root";
    private static String password;
    private static final String DATABASE = "college_event_management";

    private DB() {
    }

    public static void configureConnection(Scanner scanner) {
        String environmentUrl = System.getenv("DB_URL");
        String environmentUsername = System.getenv("DB_USER");
        String environmentPassword = System.getenv("DB_PASSWORD");

        if (environmentUrl != null && !environmentUrl.isBlank()) {
            url = environmentUrl;
        }

        if (environmentUsername != null && !environmentUsername.isBlank()) {
            username = environmentUsername;
        } else {
            username = readUsername(scanner);
        }

        if (environmentPassword != null) {
            password = environmentPassword;
        } else {
            password = readPassword(scanner);
        }
    }

    public static void initializeDatabase() throws SQLException {
        // Connect to the MySQL server first because the target database may not exist yet.
        try (Connection connection = DriverManager.getConnection(serverUrl(), username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE);
        }

        // Create tables after connecting to the new or existing database.
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS students ("
                    + "student_id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(100) NOT NULL,"
                    + "department VARCHAR(100) NOT NULL,"
                    + "semester INT NOT NULL,"
                    + "phone VARCHAR(15) NOT NULL,"
                    + "email VARCHAR(100) NOT NULL)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS events ("
                    + "event_id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "event_name VARCHAR(100) NOT NULL,"
                    + "event_type VARCHAR(50) NOT NULL,"
                    + "event_date DATE NOT NULL,"
                    + "venue VARCHAR(100) NOT NULL,"
                    + "max_capacity INT NOT NULL)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS registrations ("
                    + "registration_id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "student_id INT NOT NULL,"
                    + "event_id INT NOT NULL,"
                    + "registration_date DATE NOT NULL,"
                    + "CONSTRAINT uq_student_event UNIQUE (student_id, event_id),"
                    + "CONSTRAINT fk_registration_student FOREIGN KEY (student_id) "
                    + "REFERENCES students(student_id) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_registration_event FOREIGN KEY (event_id) "
                    + "REFERENCES events(event_id) ON DELETE CASCADE)");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl(), username, password);
    }

    private static String serverUrl() {
        int optionsIndex = url.indexOf('?');
        String options = optionsIndex >= 0 ? url.substring(optionsIndex) : "";
        String address = optionsIndex >= 0 ? url.substring(0, optionsIndex) : url;
        String databaseSuffix = "/" + DATABASE;

        if (address.endsWith(databaseSuffix)) {
            address = address.substring(0, address.length() - DATABASE.length());
        }
        if (!address.endsWith("/")) {
            address += "/";
        }
        return address + options;
    }

    private static String databaseUrl() {
        String baseUrl = serverUrl();
        int optionsIndex = baseUrl.indexOf('?');
        String options = optionsIndex >= 0 ? baseUrl.substring(optionsIndex) : "";
        String address = optionsIndex >= 0 ? baseUrl.substring(0, optionsIndex) : baseUrl;
        return address + DATABASE + options;
    }

    private static String readUsername(Scanner scanner) {
        Console console = System.console();
        String enteredUsername;
        if (console != null) {
            enteredUsername = console.readLine("MySQL username [root]: ");
        } else {
            System.out.print("MySQL username [root]: ");
            enteredUsername = scanner.nextLine();
        }
        return enteredUsername == null || enteredUsername.isBlank() ? "root" : enteredUsername.trim();
    }

    private static String readPassword(Scanner scanner) {
        Console console = System.console();
        if (console != null) {
            char[] enteredPassword = console.readPassword("MySQL password: ");
            if (enteredPassword == null) return "";
            try {
                return new String(enteredPassword);
            } finally {
                Arrays.fill(enteredPassword, '\0');
            }
        }

        System.out.print("MySQL password: ");
        return scanner.nextLine();
    }
}