package ac.tuat.fujitaken.exp.interruptibilityapp.data.base;

import java.util.Locale;

/**
 *
 * Created by hi on 2015/12/11.
 */
public class RSSI extends Data{

    private int frequency;
    public int level;
    private String mac;

    public RSSI(String s, int i, int i1){
        this.mac = s;
        this.frequency = i;
        this.level = i1;
    }

    @Override
    public String getString() {
        return mac.replaceAll(":", "") + "," + String.format(Locale.JAPAN, "%.1f", (float)frequency/1000) + "," + level;
    }
}
