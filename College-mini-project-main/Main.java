import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            DB.configureConnection(scanner);
            try {
                DB.initializeDatabase();
            } catch (SQLException exception) {
                if (exception.getErrorCode() == 1045 || "28000".equals(exception.getSQLState())) {
                    System.out.println("MySQL authentication failed.");
                    System.out.println("Set DB_USER and DB_PASSWORD, or enter the credentials when prompted.");
                } else {
                    System.out.println("Could not connect to MySQL or create the database.");
                    System.out.println("Check that MySQL is running and DB_URL is correct.");
                    System.out.println("Details: " + exception.getMessage());
                }
                return;
            }

            StudentFunctions studentFunctions = new StudentFunctions(scanner);
            EventFunctions eventFunctions = new EventFunctions(scanner);
            RegistrationFunctions registrationFunctions = new RegistrationFunctions(scanner);
            boolean running = true;

            System.out.println("College Event & Registration Management System");
            while (running) {
                System.out.println("\n1. Student Management");
                System.out.println("2. Event Management");
                System.out.println("3. Registration Management");
                System.out.println("4. Reports");
                System.out.println("5. Exit");
                int choice = readInt(scanner, "Enter choice: ");

                switch (choice) {
                    case 1 -> studentFunctions.showMenu();
                    case 2 -> eventFunctions.showMenu();
                    case 3 -> registrationFunctions.showMenu();
                    case 4 -> RegistrationFunctions.showReports(scanner);
                    case 5 -> running = false;
                    default -> System.out.println("Please enter a number from 1 to 5.");
                }
            }
        }

        System.out.println("Goodbye.");
    }

    static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (value > 0) return value;
            System.out.println("Enter a number greater than zero.");
        }
    }
}