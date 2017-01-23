package ac.tuat.fujitaken.exp.interruptibilityapp.data;

/**
 * Created by hi on 2015/12/01.
 */
public class DoubleData extends Data {
    public double value;   //保存データ

    public DoubleData(double value){
        this.value = value;
    }

    @Override
    public String getString() {
        return String.valueOf(value);
    }
}
