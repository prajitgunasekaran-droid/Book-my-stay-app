import java.util.*;

/**
 * BookMyStayApp - Use Case 8
 * Booking History & Reporting
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====== Book My Stay - Booking History ======");

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        // Initialize booking service with history tracking
        BookingServiceWithHistory bookingService = new BookingServiceWithHistory(inventory);

        // Initialize request queue
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Suite Room"));
        queue.addRequest(new Reservation("Charlie", "Single Room"));

        // Process bookings
        bookingService.processBookings(queue);

        // Admin: View Booking History
        BookingReportService reportService = new BookingReportService(bookingService.getBookingHistory());
        reportService.displayAllBookings();
        reportService.displaySummaryReport();

        System.out.println("=============================================");
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

/* ===================== INVENTORY ===================== */

class RoomInventory {

    private Map<String, Integer> inventory = new HashMap<>();

    public RoomInventory() {
        inventory.put("Single Room", 2);
        inventory.put("Suite Room", 2);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void decrement(String roomType) {
        int current = getAvailability(roomType);
        if (current > 0) inventory.put(roomType, current - 1);
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

/* ===================== BOOKING SERVICE WITH HISTORY ===================== */

class BookingServiceWithHistory {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private List<Reservation> bookingHistory = new ArrayList<>();

    public BookingServiceWithHistory(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void processBookings(BookingRequestQueue queue) {

        while (!queue.isEmpty()) {
            Reservation r = queue.getNextRequest();
            String roomType = r.getRoomType();
            String guest = r.getGuestName();

            if (inventory.getAvailability(roomType) > 0) {

                // Generate unique room ID
                String roomId = generateRoomId(roomType);
                while (allocatedRoomIds.contains(roomId)) {
                    roomId = generateRoomId(roomType);
                }

                allocatedRoomIds.add(roomId);
                r.setRoomId(roomId);
                inventory.decrement(roomType);

                bookingHistory.add(r); // Track confirmed reservation

                System.out.println("Booking CONFIRMED: " + guest +
                        " | Room Type: " + roomType +
                        " | Room ID: " + roomId);
            } else {
                System.out.println("Booking FAILED: " + guest + " | No " + roomType + " available");
            }
        }
    }

    private String generateRoomId(String roomType) {
        return roomType.substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 4);
    }

    public List<Reservation> getBookingHistory() {
        return bookingHistory;
    }
}

/* ===================== BOOKING REPORT SERVICE ===================== */

class BookingReportService {

    private List<Reservation> bookingHistory;

    public BookingReportService(List<Reservation> bookingHistory) {
        this.bookingHistory = bookingHistory;
    }

    /**
     * Display all bookings in chronological order
     */
    public void displayAllBookings() {
        System.out.println("\n--- Booking History ---\n");
        for (Reservation r : bookingHistory) {
            System.out.println("Guest: " + r.getGuestName() +
                    " | Room Type: " + r.getRoomType() +
                    " | Room ID: " + r.getRoomId());
        }
    }

    /**
     * Generate summary report (room type counts)
     */
    public void displaySummaryReport() {
        System.out.println("\n--- Booking Summary Report ---\n");

        Map<String, Integer> summary = new HashMap<>();
        for (Reservation r : bookingHistory) {
            summary.put(r.getRoomType(), summary.getOrDefault(r.getRoomType(), 0) + 1);
        }

        for (String roomType : summary.keySet()) {
            System.out.println(roomType + ": " + summary.get(roomType) + " bookings");
        }
    }
}