package ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data;

/**
 * Wi－Fiのアクセスポイント情報
 */
public class AccessPoint {
    public int id;
    public String mac;
    public String ssid;
    public int frequency;

    public AccessPoint(int id, String mac, String ssid, int frequency) {
        this.id = id;
        this.mac = mac;
        this.ssid = ssid;
        this.frequency = frequency;
    }

    public AccessPoint(String mac, String ssid, int frequency) {
        this(-1, mac, ssid, frequency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessPoint that = (AccessPoint) o;

        if (!mac.equals(that.mac)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }
}
