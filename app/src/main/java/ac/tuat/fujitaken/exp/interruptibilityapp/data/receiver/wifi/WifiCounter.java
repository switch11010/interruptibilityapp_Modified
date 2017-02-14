package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi;

/**
 *
 * Created by hi on 2015/12/15.
 */
class WifiCounter {
    private int count = 0;
    private double var = 0;
    private double mean = 0;

    WifiCounter(double value) {
        setVar(value);
    }

    void setVar(double value) {
        mean += value;
        double t = var * count + Math.pow(value - mean/(count+1), 2);
        count++;
        var = t/count;
    }

    @SuppressWarnings("UnusedDeclaration")
    public double getStd(){
        return Math.sqrt(var);
    }

    int getCount() {
        return count;
    }
}
