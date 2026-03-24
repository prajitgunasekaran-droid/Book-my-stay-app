import java.io.*;
import java.util.*;

/**
 * BookMyStayApp - Use Case 12
 * Data Persistence & System Recovery
 */
public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("====== Book My Stay - Persistence & Recovery ======");

        // Initialize persistence service
        PersistenceService persistence = new PersistenceService("hotel_data.ser");

        // Load persisted state (if exists)
        BookingServicePersistent bookingService = persistence.load();
        if (bookingService == null) {
            System.out.println("No previous data found. Initializing new system.");
            bookingService = new BookingServicePersistent();
        } else {
            System.out.println("Recovered previous system state.");
        }

        // Add some bookings
        bookingService.processBooking(new Reservation("Alice", "Single Room"));
        bookingService.processBooking(new Reservation("Bob", "Suite Room"));

        // Display current state
        System.out.println("\n--- Current Booking History ---");
        bookingService.displayBookingHistory();

        // Save state before shutdown
        persistence.save(bookingService);
        System.out.println("\nSystem state saved successfully.");
        System.out.println("=============================================");
    }
}

/* ===================== PERSISTENCE SERVICE ===================== */
class PersistenceService {

    private String filename;

    public PersistenceService(String filename) {
        this.filename = filename;
    }

    // Save system state to file
    public void save(BookingServicePersistent bookingService) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(bookingService);
        } catch (IOException e) {
            System.out.println("Failed to save state: " + e.getMessage());
        }
    }

    // Load system state from file
    public BookingServicePersistent load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (BookingServicePersistent) ois.readObject();
        } catch (FileNotFoundException e) {
            return null; // First run, no file exists
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Failed to load state: " + e.getMessage());
            return null;
        }
    }
}

/* ===================== BOOKING SERVICE WITH PERSISTENCE ===================== */
class BookingServicePersistent implements Serializable {

    private static final long serialVersionUID = 1L;

    private RoomInventoryPersistent inventory;
    private Map<String, Reservation> activeReservations;
    private Set<String> allocatedRoomIds;

    public BookingServicePersistent() {
        this.inventory = new RoomInventoryPersistent();
        this.activeReservations = new HashMap<>();
        this.allocatedRoomIds = new HashSet<>();
    }

    public void processBooking(Reservation r) {
        String guest = r.getGuestName();
        String roomType = r.getRoomType();

        if (!inventory.isValidRoomType(roomType)) {
            System.out.println("Invalid room type: " + roomType);
            return;
        }
        if (inventory.getAvailability(roomType) <= 0) {
            System.out.println("No availability for " + roomType);
            return;
        }

        // Generate unique room ID
        String roomId = roomType.substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 4);
        while (allocatedRoomIds.contains(roomId)) {
            roomId = roomType.substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 4);
        }
        allocatedRoomIds.add(roomId);
        r.setRoomId(roomId);

        // Update inventory
        inventory.decrement(roomType);

        // Record reservation
        activeReservations.put(guest, r);

        System.out.println("Booking CONFIRMED: " + guest + " | Room Type: " + roomType + " | Room ID: " + roomId);
    }

    public void displayBookingHistory() {
        if (activeReservations.isEmpty()) {
            System.out.println("No active bookings.");
            return;
        }
        for (Reservation r : activeReservations.values()) {
            System.out.println("Guest: " + r.getGuestName() + " | Room Type: " + r.getRoomType() + " | Room ID: " + r.getRoomId());
        }
    }
}

/* ===================== ROOM INVENTORY WITH PERSISTENCE ===================== */
class RoomInventoryPersistent implements Serializable {

    private static final long serialVersionUID = 1L;
    private Map<String, Integer> inventory;

    public RoomInventoryPersistent() {
        inventory = new HashMap<>();
        inventory.put("Single Room", 2);
        inventory.put("Suite Room", 2);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void decrement(String roomType) {
        int current = getAvailability(roomType);
        inventory.put(roomType, current - 1);
    }

    public boolean isValidRoomType(String roomType) {
        return inventory.containsKey(roomType);
    }
}

/* ===================== RESERVATION ===================== */
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String guestName;
    private String roomType;
    private String roomId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomId() { return roomId; }
}