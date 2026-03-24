import java.util.*;

/**
 * BookMyStayApp - Use Case 4
 * Demonstrates Room Search with Read-Only Access
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====== Book My Stay - Room Search ======");

        // Initialize Inventory
        RoomInventory inventory = new RoomInventory();

        // Create Room Objects (Domain)
        List<Room> rooms = new ArrayList<>();
        rooms.add(new SingleRoom());
        rooms.add(new DoubleRoom());
        rooms.add(new SuiteRoom());

        // Initialize Search Service
        RoomSearchService searchService = new RoomSearchService(inventory);

        // Perform Search (READ ONLY)
        searchService.searchAvailableRooms(rooms);

        System.out.println("========================================");
    }
}

/* ===================== ROOM DOMAIN ===================== */

/**
 * Abstract Room Class
 */
abstract class Room {

    private String roomType;
    private int beds;
    private double price;

    public Room(String roomType, int beds, double price) {
        this.roomType = roomType;
        this.beds = beds;
        this.price = price;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getBeds() {
        return beds;
    }

    public double getPrice() {
        return price;
    }

    public abstract void displayRoomDetails();
}

/**
 * Single Room
 */
class SingleRoom extends Room {

    public SingleRoom() {
        super("Single Room", 1, 2000.0);
    }

    public void displayRoomDetails() {
        System.out.println(getRoomType() + " | Beds: " + getBeds() + " | Price: ₹" + getPrice());
    }
}

/**
 * Double Room
 */
class DoubleRoom extends Room {

    public DoubleRoom() {
        super("Double Room", 2, 3500.0);
    }

    public void displayRoomDetails() {
        System.out.println(getRoomType() + " | Beds: " + getBeds() + " | Price: ₹" + getPrice());
    }
}

/**
 * Suite Room
 */
class SuiteRoom extends Room {

    public SuiteRoom() {
        super("Suite Room", 3, 6000.0);
    }

    public void displayRoomDetails() {
        System.out.println(getRoomType() + " | Beds: " + getBeds() + " | Price: ₹" + getPrice());
    }
}

/* ===================== INVENTORY ===================== */

/**
 * Centralized Inventory using HashMap
 */
class RoomInventory {

    private Map<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single Room", 5);
        inventory.put("Double Room", 3);
        inventory.put("Suite Room", 2);
    }

    // READ ONLY method
    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }
}

/* ===================== SEARCH SERVICE ===================== */

/**
 * Handles Read-Only Room Search
 */
class RoomSearchService {

    private RoomInventory inventory;

    public RoomSearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void searchAvailableRooms(List<Room> rooms) {

        System.out.println("\nAvailable Rooms:\n");

        for (Room room : rooms) {

            int available = inventory.getAvailability(room.getRoomType());

            // Defensive check
            if (available > 0) {
                room.displayRoomDetails();
                System.out.println("Available: " + available + "\n");
            }
        }
    }
}