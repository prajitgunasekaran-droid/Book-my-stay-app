public class BookMyStayApp {
    public static void main(String[] args) {

        Room single = new SingleRoom();
        single.displayRoomDetails();
    }
}

// Abstract class (NOT public)
abstract class Room {
    private String roomType;

    public Room(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomType() {
        return roomType;
    }

    public abstract void displayRoomDetails();
}

// Concrete class
class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room");
    }

    public void displayRoomDetails() {
        System.out.println(getRoomType());
    }
}