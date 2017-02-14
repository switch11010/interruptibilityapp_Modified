package ac.tuat.fujitaken.exp.interruptibilityapp.data.base;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by hi on 2015/12/10.
 */
public class WifiData extends Data {
    public List<RSSI> value;

    public WifiData(List<RSSI> rssiList){
        this.value = rssiList;
    }

    @Override
    public Data clone() {
        WifiData copy = (WifiData)super.clone();
        copy.value = this.value;
        this.value = new ArrayList<>();
        return copy;
    }

    @Override
    public String getString() {
        StringBuilder builder = new StringBuilder();
        for (RSSI ap: value){
            builder.append(ap.getString());
            builder.append(",");
        }
        return builder.toString();
    }
}
