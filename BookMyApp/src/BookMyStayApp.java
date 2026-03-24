import java.util.*;

/**
 * BookMyStayApp - Use Case 10
 * Booking Cancellation & Inventory Rollback
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====== Book My Stay - Booking Cancellation ======");

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        // Initialize booking service with history tracking
        BookingServiceWithCancellation bookingService = new BookingServiceWithCancellation(inventory);

        // Initialize request queue
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Suite Room"));

        // Process bookings
        bookingService.processBookings(queue);

        System.out.println("\n--- Cancelling Alice's booking ---");
        try {
            bookingService.cancelBooking("Alice");
        } catch (InvalidBookingException e) {
            System.out.println("Cancellation ERROR: " + e.getMessage());
        }

        System.out.println("\n--- Current Booking History ---");
        bookingService.displayBookingHistory();

        System.out.println("=============================================");
    }
}

/* ===================== CUSTOM EXCEPTION ===================== */

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

    public void increment(String roomType) {
        int current = getAvailability(roomType);
        inventory.put(roomType, current + 1);
    }

    public void decrement(String roomType) throws InvalidBookingException {
        int current = getAvailability(roomType);
        if (current <= 0) {
            throw new InvalidBookingException("No " + roomType + " available to allocate");
        }
        inventory.put(roomType, current - 1);
    }

    public boolean isValidRoomType(String roomType) {
        return inventory.containsKey(roomType);
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

/* ===================== BOOKING SERVICE WITH CANCELLATION ===================== */

class BookingServiceWithCancellation {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Reservation> activeReservations = new HashMap<>();
    private Stack<String> cancelledRoomIds = new Stack<>();

    public BookingServiceWithCancellation(RoomInventory inventory) {
        this.inventory = inventory;
    }

    /* Process multiple bookings from a queue */
    public void processBookings(BookingRequestQueue queue) {
        while (!queue.isEmpty()) {
            Reservation r = queue.getNextRequest();
            try {
                processBooking(r);
            } catch (InvalidBookingException e) {
                System.out.println("Booking ERROR: " + e.getMessage());
            }
        }
    }

    /* Process single booking */
    public void processBooking(Reservation r) throws InvalidBookingException {
        String guest = r.getGuestName();
        String roomType = r.getRoomType();

        // Validation
        if (guest == null || guest.isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty");
        }
        if (!inventory.isValidRoomType(roomType)) {
            throw new InvalidBookingException("Invalid room type: " + roomType);
        }
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

        // Record active reservation
        activeReservations.put(guest, r);

        System.out.println("Booking CONFIRMED: " + guest +
                " | Room Type: " + roomType +
                " | Room ID: " + roomId);
    }

    /* Cancel a confirmed booking */
    public void cancelBooking(String guestName) throws InvalidBookingException {
        if (!activeReservations.containsKey(guestName)) {
            throw new InvalidBookingException("No active reservation found for guest: " + guestName);
        }

        Reservation r = activeReservations.remove(guestName);
        String roomId = r.getRoomId();
        String roomType = r.getRoomType();

        // Restore inventory
        inventory.increment(roomType);

        // Track cancelled room IDs
        cancelledRoomIds.push(roomId);

        // Remove allocation
        allocatedRoomIds.remove(roomId);

        System.out.println("Booking CANCELLED: " + guestName +
                " | Room Type: " + roomType +
                " | Room ID: " + roomId);
    }

    /* Display active bookings */
    public void displayBookingHistory() {
        if (activeReservations.isEmpty()) {
            System.out.println("No active bookings.");
            return;
        }
        for (Reservation r : activeReservations.values()) {
            System.out.println("Guest: " + r.getGuestName() +
                    " | Room Type: " + r.getRoomType() +
                    " | Room ID: " + r.getRoomId());
        }
    }

    private String generateRoomId(String roomType) {
        return roomType.substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 4);
    }
}