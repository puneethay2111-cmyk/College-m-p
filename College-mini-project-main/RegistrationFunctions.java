import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class RegistrationFunctions {
    private final Scanner scanner;

    public RegistrationFunctions(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\nRegistration Management");
            System.out.println("1. Register Student for Event\n2. View All Registrations");
            System.out.println("3. Cancel Registration\n4. View Event Participants\n5. Back");
            int choice = Main.readInt(scanner, "Enter choice: ");
            switch (choice) {
                case 1 -> registerStudent();
                case 2 -> viewRegistrations();
                case 3 -> cancelRegistration();
                case 4 -> viewParticipants();
                case 5 -> back = true;
                default -> System.out.println("Please enter a number from 1 to 5.");
            }
        }
    }

    private void registerStudent() {
        int studentId = Main.readInt(scanner, "Student ID: ");
        int eventId = Main.readInt(scanner, "Event ID: ");
        String studentCheck = "SELECT student_id FROM students WHERE student_id = ?";
        String eventCheck = "SELECT max_capacity FROM events WHERE event_id = ?";
        String duplicateCheck = "SELECT registration_id FROM registrations WHERE student_id = ? AND event_id = ?";
        String countCheck = "SELECT COUNT(*) FROM registrations WHERE event_id = ?";

        try (Connection connection = DB.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(studentCheck)) {
                statement.setInt(1, studentId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        System.out.println("Student not found.");
                        return;
                    }
                }
            }

            int capacity;
            try (PreparedStatement statement = connection.prepareStatement(eventCheck)) {
                statement.setInt(1, eventId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        System.out.println("Event not found.");
                        return;
                    }
                    capacity = result.getInt("max_capacity");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(duplicateCheck)) {
                statement.setInt(1, studentId);
                statement.setInt(2, eventId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        System.out.println("Student is already registered for this event.");
                        return;
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(countCheck)) {
                statement.setInt(1, eventId);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    if (result.getInt(1) >= capacity) {
                        System.out.println("Event is full.");
                        return;
                    }
                }
            }

            Registration registration = new Registration(studentId, eventId, LocalDate.now());
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO registrations (student_id, event_id, registration_date) VALUES (?, ?, ?)")) {
                statement.setInt(1, registration.getStudentId());
                statement.setInt(2, registration.getEventId());
                statement.setDate(3, Date.valueOf(registration.getRegistrationDate()));
                statement.executeUpdate();
                System.out.println("Registration successful.");
            }
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private void viewRegistrations() {
        String sql = "SELECT r.registration_id, r.student_id, s.name AS student_name, "
                + "r.event_id, e.event_name, r.registration_date "
                + "FROM registrations r JOIN students s ON r.student_id = s.student_id "
                + "JOIN events e ON r.event_id = e.event_id ORDER BY r.registration_id";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            printRegistrations(results);
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private void cancelRegistration() {
        int id = Main.readInt(scanner, "Registration ID to cancel: ");
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM registrations WHERE registration_id = ?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() > 0) System.out.println("Registration cancelled successfully.");
            else System.out.println("Registration not found.");
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private void viewParticipants() {
        int eventId = Main.readInt(scanner, "Event ID: ");
        showParticipants(eventId);
    }

    private static void showParticipants(int eventId) {
        String sql = "SELECT s.* FROM students s JOIN registrations r ON s.student_id = r.student_id "
                + "WHERE r.event_id = ? ORDER BY s.student_id";
        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            try (ResultSet results = statement.executeQuery()) {
                StudentFunctions.printStudents(results);
            }
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    static void showReports(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\nReports");
            System.out.println("1. Display all students\n2. Display all events\n3. Display all registrations");
            System.out.println("4. Participants of an event\n5. Events registered by a student");
            System.out.println("6. Participant count for each event\n7. Back");
            int choice = Main.readInt(scanner, "Enter choice: ");
            switch (choice) {
                case 1 -> runStudentReport("SELECT * FROM students ORDER BY student_id");
                case 2 -> runEventReport("SELECT * FROM events ORDER BY event_id");
                case 3 -> runRegistrationReport();
                case 4 -> showParticipants(Main.readInt(scanner, "Event ID: "));
                case 5 -> showStudentEvents(Main.readInt(scanner, "Student ID: "));
                case 6 -> showParticipantCounts();
                case 7 -> back = true;
                default -> System.out.println("Please enter a number from 1 to 7.");
            }
        }
    }

    private static void runStudentReport(String sql) {
        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            StudentFunctions.printStudents(results);
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private static void runEventReport(String sql) {
        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            EventFunctions.printEvents(results);
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private static void runRegistrationReport() {
        String sql = "SELECT r.registration_id, r.student_id, s.name AS student_name, "
                + "r.event_id, e.event_name, r.registration_date "
                + "FROM registrations r JOIN students s ON r.student_id = s.student_id "
                + "JOIN events e ON r.event_id = e.event_id ORDER BY r.registration_id";
        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            printRegistrations(results);
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private static void showStudentEvents(int studentId) {
        String sql = "SELECT e.* FROM events e JOIN registrations r ON e.event_id = r.event_id "
                + "WHERE r.student_id = ? ORDER BY e.event_date";
        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet results = statement.executeQuery()) {
                EventFunctions.printEvents(results);
            }
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private static void showParticipantCounts() {
        String sql = "SELECT e.event_id, e.event_name, e.max_capacity, COUNT(r.registration_id) AS participant_count "
                + "FROM events e LEFT JOIN registrations r ON e.event_id = r.event_id "
                + "GROUP BY e.event_id, e.event_name, e.max_capacity ORDER BY e.event_id";
        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            boolean found = false;
            while (results.next()) {
                found = true;
                System.out.printf("Event %d: %s | Participants: %d/%d%n", results.getInt("event_id"),
                        results.getString("event_name"), results.getInt("participant_count"),
                        results.getInt("max_capacity"));
            }
            if (!found) System.out.println("No events found.");
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private static void printRegistrations(ResultSet results) throws SQLException {
        boolean found = false;
        while (results.next()) {
            found = true;
            System.out.printf("Registration: %d | Student: %d (%s) | Event: %d (%s) | Date: %s%n",
                    results.getInt("registration_id"), results.getInt("student_id"),
                    results.getString("student_name"), results.getInt("event_id"),
                    results.getString("event_name"), results.getDate("registration_date"));
        }
        if (!found) System.out.println("No registrations found.");
    }
}