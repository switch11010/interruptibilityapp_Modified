package ac.tuat.fujitaken.exp.interruptibilityapp.receiver.wifi.data;

/**
 * 場所
 */
public class Spot {
    public int id;
    public String name;
    public double latitude;
    public double longitude;

    public Spot(int id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
