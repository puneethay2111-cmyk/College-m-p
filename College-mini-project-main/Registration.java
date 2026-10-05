import java.time.LocalDate;

public class Registration {
    private final int studentId;
    private final int eventId;
    private final LocalDate registrationDate;

    public Registration(int studentId, int eventId, LocalDate registrationDate) {
        this.studentId = studentId;
        this.eventId = eventId;
        this.registrationDate = registrationDate;
    }

    public int getStudentId() { return studentId; }
    public int getEventId() { return eventId; }
    public LocalDate getRegistrationDate() { return registrationDate; }
}