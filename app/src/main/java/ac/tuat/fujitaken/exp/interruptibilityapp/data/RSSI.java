package ac.tuat.fujitaken.exp.interruptibilityapp.data;

/**
 *
 * Created by hi on 2015/12/11.
 */
public class RSSI extends Data{

    public int frequency,
            level;
    public String mac;

    public RSSI(String mac, int frequency, int level){
        this.mac = mac;
        this.frequency = frequency;
        this.level = level;
    }

    @Override
    public String getString() {
        return mac.replaceAll(":", "") + "," + String.format("%.1f", (float)frequency/1000) + "," + level;
    }
}
