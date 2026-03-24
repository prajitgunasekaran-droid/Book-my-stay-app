import java.util.*;

/**
 * BookMyStayApp - Use Case 7
 * Demonstrates Add-On Service Selection for confirmed bookings
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====== Book My Stay - Add-On Services ======");

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        // Initialize booking service
        BookingService bookingService = new BookingService(inventory);

        // Add booking requests
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Suite Room"));

        // Confirm bookings
        bookingService.processBookings(queue);

        // Initialize Add-On Manager
        AddOnServiceManager addOnManager = new AddOnServiceManager();

        // Add services to reservations
        addOnManager.addService(bookingService.getReservationRoomId("Alice"), new AddOnService("Breakfast", 500));
        addOnManager.addService(bookingService.getReservationRoomId("Alice"), new AddOnService("Spa", 1500));
        addOnManager.addService(bookingService.getReservationRoomId("Bob"), new AddOnService("Airport Pickup", 800));

        // Display selected services
        addOnManager.displayServices();
        System.out.println("=============================================");
    }
}

/* ===================== ADD-ON SERVICE ===================== */

/**
 * Represents an optional service for a reservation
 */
class AddOnService {

    private String name;
    private double cost;

    public AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public void displayService() {
        System.out.println(name + " | Cost: ₹" + cost);
    }
}

/* ===================== ADD-ON SERVICE MANAGER ===================== */

/**
 * Manages mapping of reservation IDs to selected services
 */
class AddOnServiceManager {

    private Map<String, List<AddOnService>> reservationServices = new HashMap<>();

    /**
     * Add a service to a reservation
     */
    public void addService(String reservationId, AddOnService service) {
        reservationServices
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);
        System.out.println("Added service '" + service.getName() + "' to reservation " + reservationId);
    }

    /**
     * Display all services per reservation and total additional cost
     */
    public void displayServices() {
        System.out.println("\nReservation Add-On Services:\n");
        for (String resId : reservationServices.keySet()) {
            System.out.println("Reservation ID: " + resId);
            double totalCost = 0;
            for (AddOnService s : reservationServices.get(resId)) {
                s.displayService();
                totalCost += s.getCost();
            }
            System.out.println("Total Additional Cost: ₹" + totalCost + "\n");
        }
    }
}

/* ===================== ROOM INVENTORY & BOOKING (Simplified) ===================== */

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

class BookingService {

    private RoomInventory inventory;
    private Map<String, String> reservationToRoomId = new HashMap<>();
    private Set<String> allocatedRoomIds = new HashSet<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void processBookings(BookingRequestQueue queue) {
        while (!queue.isEmpty()) {
            Reservation r = queue.getNextRequest();
            String roomType = r.getRoomType();
            String guest = r.getGuestName();
            if (inventory.getAvailability(roomType) > 0) {
                String roomId = generateRoomId(roomType);
                while (allocatedRoomIds.contains(roomId)) {
                    roomId = generateRoomId(roomType);
                }
                allocatedRoomIds.add(roomId);
                reservationToRoomId.put(guest, roomId);
                inventory.decrement(roomType);
                System.out.println("Booking CONFIRMED: " + guest + " | Room ID: " + roomId);
            } else {
                System.out.println("Booking FAILED: " + guest + " | No " + roomType + " available");
            }
        }
    }

    private String generateRoomId(String roomType) {
        return roomType.substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 4);
    }

    public String getReservationRoomId(String guestName) {
        return reservationToRoomId.get(guestName);
    }
}