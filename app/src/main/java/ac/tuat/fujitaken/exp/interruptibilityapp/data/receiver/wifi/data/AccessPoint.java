package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

/**
 * Wi－Fiのアクセスポイント情報
 */
public class AccessPoint {
    public int id;
    public String mac;
    public String ssid;
    public int frequency;

    public AccessPoint(int i, String s, String s1, int i1) {
        this.id = i;
        this.mac = s;
        this.ssid = s1;
        this.frequency = i1;
    }

    public AccessPoint(String s, String s1, int i) {
        this(-1, s, s1, i);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccessPoint that = (AccessPoint) o;

        return mac.equals(that.mac);

    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }
}
