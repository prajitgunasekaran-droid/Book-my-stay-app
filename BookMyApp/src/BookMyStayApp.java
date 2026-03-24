import java.util.*;

/**
 * BookMyStayApp - Use Case 6
 * Reservation Confirmation & Safe Allocation
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====== Book My Stay - Booking Processing ======");

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        // Initialize queue
        BookingRequestQueue requestQueue = new BookingRequestQueue();

        // Add booking requests
        requestQueue.addRequest(new Reservation("Alice", "Single Room"));
        requestQueue.addRequest(new Reservation("Bob", "Single Room"));
        requestQueue.addRequest(new Reservation("Charlie", "Suite Room"));
        requestQueue.addRequest(new Reservation("David", "Suite Room"));
        requestQueue.addRequest(new Reservation("Eve", "Suite Room")); // may fail

        // Process bookings
        BookingService bookingService = new BookingService(inventory);
        bookingService.processBookings(requestQueue);

        System.out.println("===============================================");
    }
}

/* ===================== RESERVATION ===================== */

class Reservation {

    private String guestName;
    private String roomType;

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
}

/* ===================== QUEUE ===================== */

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
    }

    public Reservation getNextRequest() {
        return queue.poll(); // FIFO removal
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

/* ===================== INVENTORY ===================== */

class RoomInventory {

    private Map<String, Integer> inventory = new HashMap<>();

    public RoomInventory() {
        inventory.put("Single Room", 2);
        inventory.put("Double Room", 2);
        inventory.put("Suite Room", 2);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void decrement(String roomType) {
        int current = getAvailability(roomType);
        if (current > 0) {
            inventory.put(roomType, current - 1);
        }
    }
}

/* ===================== BOOKING SERVICE ===================== */

class BookingService {

    private RoomInventory inventory;

    // Track all allocated room IDs (global uniqueness)
    private Set<String> allocatedRoomIds = new HashSet<>();

    // Map room type → allocated room IDs
    private Map<String, Set<String>> roomAllocations = new HashMap<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Process all booking requests
     */
    public void processBookings(BookingRequestQueue queue) {

        System.out.println("\nProcessing Booking Requests...\n");

        while (!queue.isEmpty()) {

            Reservation request = queue.getNextRequest();

            String roomType = request.getRoomType();
            String guest = request.getGuestName();

            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                // Generate unique room ID
                String roomId = generateRoomId(roomType);

                // Ensure uniqueness (Set check)
                while (allocatedRoomIds.contains(roomId)) {
                    roomId = generateRoomId(roomType);
                }

                // Record allocation
                allocatedRoomIds.add(roomId);

                roomAllocations
                        .computeIfAbsent(roomType, k -> new HashSet<>())
                        .add(roomId);

                // Update inventory immediately (atomic step)
                inventory.decrement(roomType);

                // Confirm booking
                System.out.println("Booking CONFIRMED for " + guest +
                        " | Room Type: " + roomType +
                        " | Room ID: " + roomId);

            } else {
                System.out.println("Booking FAILED for " + guest +
                        " | No " + roomType + " available");
            }
        }
    }

    /**
     * Generate unique room ID
     */
    private String generateRoomId(String roomType) {
        return roomType.substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 4);
    }
}