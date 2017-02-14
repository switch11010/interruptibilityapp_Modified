package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

/**
 * 場所
 */
public class Spot {
    public int id;
    public String name;
    public double latitude;
    public double longitude;

    public Spot(int i, String s, double v, double v1) {
        this.id = i;
        this.name = s;
        this.latitude = v;
        this.longitude = v1;
    }
}
