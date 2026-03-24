import java.util.*;

/**
 * BookMyStayApp - Use Case 9
 * Error Handling & Validation for bookings
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====== Book My Stay - Error Handling ======");

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        // Initialize booking service with validation
        BookingServiceWithValidation bookingService = new BookingServiceWithValidation(inventory);

        // Initialize request queue
        BookingRequestQueue queue = new BookingRequestQueue();

        // Valid booking request
        queue.addRequest(new Reservation("Alice", "Single Room"));

        // Invalid booking request (room type does not exist)
        queue.addRequest(new Reservation("Bob", "Penthouse"));

        // Process bookings with validation
        while (!queue.isEmpty()) {
            Reservation r = queue.getNextRequest();
            try {
                bookingService.processBooking(r);
            } catch (InvalidBookingException e) {
                System.out.println("Booking ERROR: " + e.getMessage());
            }
        }

        System.out.println("=============================================");
    }
}

/* ===================== CUSTOM EXCEPTION ===================== */

/**
 * Domain-specific exception for invalid bookings
 */
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

/* ===================== ROOM INVENTORY ===================== */

class RoomInventory {

    private Map<String, Integer> inventory = new HashMap<>();

    public RoomInventory() {
        inventory.put("Single Room", 2);
        inventory.put("Suite Room", 2);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void decrement(String roomType) throws InvalidBookingException {
        int current = getAvailability(roomType);
        if (current <= 0) {
            throw new InvalidBookingException("Cannot decrement inventory: No " + roomType + " available");
        }
        inventory.put(roomType, current - 1);
    }

    public boolean isValidRoomType(String roomType) {
        return inventory.containsKey(roomType);
    }
}

/* ===================== BOOKING SERVICE WITH VALIDATION ===================== */

class BookingServiceWithValidation {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private List<Reservation> bookingHistory = new ArrayList<>();

    public BookingServiceWithValidation(RoomInventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Process a single booking with validation
     */
    public void processBooking(Reservation r) throws InvalidBookingException {
        String guest = r.getGuestName();
        String roomType = r.getRoomType();

        // Fail-fast validation
        if (guest == null || guest.isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty");
        }
        if (!inventory.isValidRoomType(roomType)) {
            throw new InvalidBookingException("Invalid room type: " + roomType);
        }

        // Check availability
        if (inventory.getAvailability(roomType) <= 0) {
            throw new InvalidBookingException("No " + roomType + " available for " + guest);
        }

        // Allocate room
        String roomId = generateRoomId(roomType);
        while (allocatedRoomIds.contains(roomId)) {
            roomId = generateRoomId(roomType);
        }
        allocatedRoomIds.add(roomId);
        r.setRoomId(roomId);

        // Update inventory
        inventory.decrement(roomType);

        // Record confirmed booking
        bookingHistory.add(r);

        System.out.println("Booking CONFIRMED: " + guest +
                " | Room Type: " + roomType +
                " | Room ID: " + roomId);
    }

    private String generateRoomId(String roomType) {
        return roomType.substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 4);
    }

    public List<Reservation> getBookingHistory() {
        return bookingHistory;
    }
}

/* ===================== BOOKING REQUEST QUEUE ===================== */

class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
    }

    public Reservation getNextRequest() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

/* ===================== RESERVATION ===================== */

class Reservation {
    private String guestName;
    private String roomType;
    private String roomId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}