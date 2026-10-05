import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class EventFunctions {
    private final Scanner scanner;

    public EventFunctions(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\nEvent Management");
            System.out.println("1. Add Event\n2. View All Events\n3. Search Event");
            System.out.println("4. Update Event\n5. Delete Event\n6. Back");
            int choice = Main.readInt(scanner, "Enter choice: ");
            switch (choice) {
                case 1 -> addEvent();
                case 2 -> viewEvents();
                case 3 -> searchEvent();
                case 4 -> updateEvent();
                case 5 -> deleteEvent();
                case 6 -> back = true;
                default -> System.out.println("Please enter a number from 1 to 6.");
            }
        }
    }

    private void addEvent() {
        Event event = readEventDetails();
        String sql = "INSERT INTO events (event_name, event_type, event_date, venue, max_capacity) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            setEventValues(statement, event, 1);
            statement.executeUpdate();
            System.out.println("Event created successfully.");
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private void viewEvents() {
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM events ORDER BY event_id");
             ResultSet results = statement.executeQuery()) {
            printEvents(results);
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private void searchEvent() {
        int id = Main.readInt(scanner, "Event ID: ");
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM events WHERE event_id = ?")) {
            statement.setInt(1, id);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) printEvent(results);
                else System.out.println("Event not found.");
            }
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private void updateEvent() {
        int id = Main.readInt(scanner, "Event ID to update: ");
        Event event = readEventDetails();
        String sql = "UPDATE events SET event_name = ?, event_type = ?, event_date = ?, venue = ?, max_capacity = ? WHERE event_id = ?";
        try (Connection connection = DB.getConnection()) {
            try (PreparedStatement countStatement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM registrations WHERE event_id = ?")) {
                countStatement.setInt(1, id);
                try (ResultSet result = countStatement.executeQuery()) {
                    result.next();
                    if (event.getMaxCapacity() < result.getInt(1)) {
                        System.out.println("Capacity cannot be lower than the current participant count.");
                        return;
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setEventValues(statement, event, 1);
                statement.setInt(6, id);
                if (statement.executeUpdate() > 0) System.out.println("Event updated successfully.");
                else System.out.println("Event not found.");
            }
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private void deleteEvent() {
        int id = Main.readInt(scanner, "Event ID to delete: ");
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM events WHERE event_id = ?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() > 0) System.out.println("Event deleted successfully.");
            else System.out.println("Event not found.");
        } catch (SQLException exception) {
            StudentFunctions.showDatabaseError(exception);
        }
    }

    private Event readEventDetails() {
        String name = readText("Event name: ");
        String type = readText("Event type: ");
        LocalDate date = readDate("Event date (YYYY-MM-DD): ");
        String venue = readText("Venue: ");
        int capacity = Main.readPositiveInt(scanner, "Maximum capacity: ");
        return new Event(name, type, date, venue, capacity);
    }

    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException exception) {
                System.out.println("Use the date format YYYY-MM-DD.");
            }
        }
    }

    private static void setEventValues(PreparedStatement statement, Event event, int firstIndex) throws SQLException {
        statement.setString(firstIndex, event.getEventName());
        statement.setString(firstIndex + 1, event.getEventType());
        statement.setDate(firstIndex + 2, Date.valueOf(event.getEventDate()));
        statement.setString(firstIndex + 3, event.getVenue());
        statement.setInt(firstIndex + 4, event.getMaxCapacity());
    }

    static void printEvents(ResultSet results) throws SQLException {
        boolean found = false;
        while (results.next()) {
            found = true;
            printEvent(results);
        }
        if (!found) System.out.println("No events found.");
    }

    static void printEvent(ResultSet result) throws SQLException {
        System.out.printf("ID: %d | Name: %s | Type: %s | Date: %s | Venue: %s | Capacity: %d%n",
                result.getInt("event_id"), result.getString("event_name"), result.getString("event_type"),
                result.getDate("event_date"), result.getString("venue"), result.getInt("max_capacity"));
    }
}