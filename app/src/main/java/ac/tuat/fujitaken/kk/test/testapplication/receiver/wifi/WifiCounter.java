package ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi;

/**
 *
 * Created by hi on 2015/12/15.
 */
public class WifiCounter {
    private int count = 0;
    private double var = 0;
    private double mean = 0;

    public WifiCounter(double value) {
        setVar(value);
    }

    public void setVar(double value) {
        mean += value;
        double t = var * count + Math.pow(value - mean/(count+1), 2);
        count++;
        var = t/count;
    }

    public double getStd(){
        return Math.sqrt(var);
    }

    public int getCount() {
        return count;
    }
}
