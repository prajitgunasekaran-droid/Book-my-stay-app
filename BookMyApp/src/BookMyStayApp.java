import java.util.*;
import java.util.concurrent.*;

/**
 * BookMyStayApp - Use Case 11
 * Concurrent Booking Simulation with Thread Safety
 */
public class BookMyStayApp {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("====== Book My Stay - Concurrent Booking Simulation ======");

        // Shared inventory
        RoomInventory inventory = new RoomInventory();

        // Shared booking queue
        BookingRequestQueue queue = new BookingRequestQueue();

        // Sample concurrent booking requests
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Suite Room"));
        queue.addRequest(new Reservation("Charlie", "Single Room"));
        queue.addRequest(new Reservation("David", "Suite Room"));
        queue.addRequest(new Reservation("Eve", "Single Room"));

        // Booking service with synchronized allocation
        BookingServiceConcurrent bookingService = new BookingServiceConcurrent(inventory);

        // Create threads simulating multiple guests booking concurrently
        int numThreads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                while (!queue.isEmpty()) {
                    Reservation r = queue.getNextRequest();
                    if (r != null) {
                        try {
                            bookingService.processBooking(r);
                        } catch (InvalidBookingException e) {
                            System.out.println("Booking ERROR: " + e.getMessage());
                        }
                    }
                }
            });
        }

        // Shutdown executor and wait for tasks to finish
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("\n--- Final Booking History ---");
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

    // Synchronized access to ensure thread safety
    public synchronized int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public synchronized void decrement(String roomType) throws InvalidBookingException {
        int current = getAvailability(roomType);
        if (current <= 0) {
            throw new InvalidBookingException("No " + roomType + " available to allocate");
        }
        inventory.put(roomType, current - 1);
    }

    public synchronized void increment(String roomType) {
        int current = getAvailability(roomType);
        inventory.put(roomType, current + 1);
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

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomId() { return roomId; }
}

/* ===================== BOOKING REQUEST QUEUE ===================== */
class BookingRequestQueue {
    private Queue<Reservation> queue = new ConcurrentLinkedQueue<>();

    public void addRequest(Reservation r) { queue.offer(r); }

    public Reservation getNextRequest() { return queue.poll(); }

    public boolean isEmpty() { return queue.isEmpty(); }
}

/* ===================== CONCURRENT BOOKING SERVICE ===================== */
class BookingServiceConcurrent {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds = Collections.synchronizedSet(new HashSet<>());
    private Map<String, Reservation> activeReservations = new ConcurrentHashMap<>();

    public BookingServiceConcurrent(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void processBooking(Reservation r) throws InvalidBookingException {

        synchronized (this) { // Critical section for allocation
            String guest = r.getGuestName();
            String roomType = r.getRoomType();

            if (guest == null || guest.isEmpty()) {
                throw new InvalidBookingException("Guest name cannot be empty");
            }
            if (!inventory.isValidRoomType(roomType)) {
                throw new InvalidBookingException("Invalid room type: " + roomType);
            }
            if (inventory.getAvailability(roomType) <= 0) {
                throw new InvalidBookingException("No " + roomType + " available for " + guest);
            }

            // Allocate unique room ID
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
    }

    private String generateRoomId(String roomType) {
        return roomType.substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 4);
    }

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
}