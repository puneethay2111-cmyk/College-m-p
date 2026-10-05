import java.time.LocalDate;

public class Event {
    private final String eventName;
    private final String eventType;
    private final LocalDate eventDate;
    private final String venue;
    private final int maxCapacity;

    public Event(String eventName, String eventType, LocalDate eventDate, String venue, int maxCapacity) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.venue = venue;
        this.maxCapacity = maxCapacity;
    }

    public String getEventName() { return eventName; }
    public String getEventType() { return eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public String getVenue() { return venue; }
    public int getMaxCapacity() { return maxCapacity; }
}