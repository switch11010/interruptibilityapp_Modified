package ac.tuat.fujitaken.exp.interruptibilityapp.data.base;

/**
 * Double型用
 * Created by hi on 2015/12/01.
 */
@SuppressWarnings("UnusedDeclaration")
public class DoubleData extends Data {
    public double value;   //保存データ

    public DoubleData(double v){
        this.value = v;
    }

    @Override
    public String getString() {
        return String.valueOf(value);
    }
}
