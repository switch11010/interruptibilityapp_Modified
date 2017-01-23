package ac.tuat.fujitaken.exp.interruptibilityapp.receiver.wifi.data;

/**
 * 各場所のWiFiアクセスポイントの電波強度パターン
 */
public class Pattern {
    public int apid;
    public int spotid;
    public double averageLevel;
    public int sampleCount;

    public Pattern(int apid, int spotid, double averageLevel, int sampleCount) {
        this.apid = apid;
        this.spotid = spotid;
        this.averageLevel = averageLevel;
        this.sampleCount = sampleCount;
    }
}
