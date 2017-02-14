package ac.tuat.fujitaken.exp.interruptibilityapp.data.base;

/**
 * int型のデータを保持するデータ型
 * Created by hi on 2015/11/10.
 */
public class IntData extends Data {
    public int value;   //保存データ
    private static final IntData ZERO = new IntData(0);

    public IntData(int i){
        this.value = i;
    }

    @Override
    public Data clone() {
        if(value == 0){
            return ZERO;
        }
        else {
            return super.clone();
        }
    }

    @Override
    public String getString() {
        return String.valueOf(value);
    }
}
