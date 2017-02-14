package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

/**
 * 各場所のWiFiアクセスポイントの電波強度パターン
 */
public class Pattern {
    public int apid;
    public int spotid;
    public double averageLevel;
    public int sampleCount;

    public Pattern(int i, int i1, double v, int i2) {
        this.apid = i;
        this.spotid = i1;
        this.averageLevel = v;
        this.sampleCount = i2;
    }
}
