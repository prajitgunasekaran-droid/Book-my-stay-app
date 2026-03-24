import java.util.*;

/**
 * BookMyStayApp - Use Case 5
 * Demonstrates Booking Request Queue (FIFO)
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====== Book My Stay - Booking Requests ======");

        // Initialize Queue
        BookingRequestQueue requestQueue = new BookingRequestQueue();

        // Simulate booking requests
        requestQueue.addRequest(new Reservation("Alice", "Single Room"));
        requestQueue.addRequest(new Reservation("Bob", "Double Room"));
        requestQueue.addRequest(new Reservation("Charlie", "Suite Room"));
        requestQueue.addRequest(new Reservation("David", "Single Room"));

        // Display queue (FIFO order)
        requestQueue.displayQueue();

        System.out.println("=============================================");
    }
}

/* ===================== RESERVATION ===================== */

/**
 * Represents a booking request
 */
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

    public void displayReservation() {
        System.out.println("Guest: " + guestName + " | Requested: " + roomType);
    }
}

/* ===================== QUEUE ===================== */

/**
 * BookingRequestQueue
 *
 * Maintains booking requests in FIFO order
 */
class BookingRequestQueue {

    private Queue<Reservation> queue;

    public BookingRequestQueue() {
        queue = new LinkedList<>();
    }

    /**
     * Add request to queue
     */
    public void addRequest(Reservation reservation) {
        queue.offer(reservation);
        System.out.println("Request added for " + reservation.getGuestName());
    }

    /**
     * View next request (without removing)
     */
    public Reservation peekNextRequest() {
        return queue.peek();
    }

    /**
     * Display all requests in order
     */
    public void displayQueue() {

        System.out.println("\nBooking Request Queue (FIFO Order):\n");

        for (Reservation r : queue) {
            r.displayReservation();
        }
    }
}