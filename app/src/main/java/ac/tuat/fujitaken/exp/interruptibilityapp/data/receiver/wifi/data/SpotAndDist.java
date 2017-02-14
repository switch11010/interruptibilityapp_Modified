package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

/**
 * 場所と距離のデータ用
 * Created by seuo on 15/06/30.
 */
public class SpotAndDist {
    private double Dist;
    private int SpotId;

    public SpotAndDist(double v, int i){
        this.Dist = v;
        this.SpotId = i;
    }

    public double getDist() {
        return Dist;
    }

    public int getSpotId() {
        return SpotId;
    }
}
