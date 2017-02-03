package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

/**
 * Created by seuo on 15/06/30.
 */
public class SpotAndDist {
    private double Dist;
    private int SpotId;

    public SpotAndDist(double Dist, int SpotId){
        this.Dist = Dist;
        this.SpotId = SpotId;
    }

    public double getDist() {
        return Dist;
    }

    public int getSpotId() {
        return SpotId;
    }
}
