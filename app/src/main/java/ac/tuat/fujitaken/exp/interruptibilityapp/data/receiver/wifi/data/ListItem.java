package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

/**
 * APリストアイテム
 * Created by seuo on 15/06/30.
 */
public class ListItem {

    AccessPoint ap;
    public Pattern pattern;

    public ListItem(AccessPoint accessPoint, Pattern p){
        this.ap = accessPoint;
        this.pattern = p;
    }
}
