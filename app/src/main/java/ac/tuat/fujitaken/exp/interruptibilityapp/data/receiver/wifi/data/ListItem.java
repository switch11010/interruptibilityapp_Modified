package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

/**
 * Created by seuo on 15/06/30.
 */
public class ListItem {

    public AccessPoint ap;
    public Pattern pattern;

    public ListItem(AccessPoint ap, Pattern pattern){
        this.ap = ap;
        this.pattern = pattern;
    }
}
