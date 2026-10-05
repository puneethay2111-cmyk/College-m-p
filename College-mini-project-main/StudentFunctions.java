import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class StudentFunctions {
    private final Scanner scanner;

    public StudentFunctions(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\nStudent Management");
            System.out.println("1. Add Student\n2. View All Students\n3. Search Student");
            System.out.println("4. Update Student\n5. Delete Student\n6. Back");
            int choice = Main.readInt(scanner, "Enter choice: ");
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewStudents();
                case 3 -> searchStudent();
                case 4 -> updateStudent();
                case 5 -> deleteStudent();
                case 6 -> back = true;
                default -> System.out.println("Please enter a number from 1 to 6.");
            }
        }
    }

    private void addStudent() {
        String name = readText("Name: ");
        String department = readText("Department: ");
        int semester = Main.readPositiveInt(scanner, "Semester: ");
        String phone = readText("Phone: ");
        String email = readText("Email: ");
        Student student = new Student(name, department, semester, phone, email);
        String sql = "INSERT INTO students (name, department, semester, phone, email) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.getName());
            statement.setString(2, student.getDepartment());
            statement.setInt(3, student.getSemester());
            statement.setString(4, student.getPhone());
            statement.setString(5, student.getEmail());
            statement.executeUpdate();
            System.out.println("Student added successfully.");
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void viewStudents() {
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM students ORDER BY student_id");
             ResultSet results = statement.executeQuery()) {
            printStudents(results);
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void searchStudent() {
        int id = Main.readInt(scanner, "Student ID: ");
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM students WHERE student_id = ?")) {
            statement.setInt(1, id);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) printStudent(results);
                else System.out.println("Student not found.");
            }
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void updateStudent() {
        int id = Main.readInt(scanner, "Student ID to update: ");
        String sql = "UPDATE students SET name = ?, department = ?, semester = ?, phone = ?, email = ? WHERE student_id = ?";
        try (Connection connection = DB.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, readText("New name: "));
            statement.setString(2, readText("New department: "));
            statement.setInt(3, Main.readPositiveInt(scanner, "New semester: "));
            statement.setString(4, readText("New phone: "));
            statement.setString(5, readText("New email: "));
            statement.setInt(6, id);
            if (statement.executeUpdate() > 0) System.out.println("Student updated successfully.");
            else System.out.println("Student not found.");
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void deleteStudent() {
        int id = Main.readInt(scanner, "Student ID to delete: ");
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE student_id = ?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() > 0) System.out.println("Student deleted successfully.");
            else System.out.println("Student not found.");
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    static void printStudents(ResultSet results) throws SQLException {
        boolean found = false;
        while (results.next()) {
            found = true;
            printStudent(results);
        }
        if (!found) System.out.println("No students found.");
    }

    static void printStudent(ResultSet result) throws SQLException {
        System.out.printf("ID: %d | Name: %s | Department: %s | Semester: %d | Phone: %s | Email: %s%n",
                result.getInt("student_id"), result.getString("name"), result.getString("department"),
                result.getInt("semester"), result.getString("phone"), result.getString("email"));
    }

    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    static void showDatabaseError(SQLException exception) {
        System.out.println("Database operation failed: " + exception.getMessage());
    }
}