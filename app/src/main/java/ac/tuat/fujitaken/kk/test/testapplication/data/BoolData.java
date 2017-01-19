package ac.tuat.fujitaken.kk.test.testapplication.data;

/**
 * ブーリアン型のデータを保持するデータ型
 * Created by hi on 2015/11/11.
 */
public class BoolData extends Data {
    public boolean value;   //保存データ
    private static final BoolData TRUE = new BoolData(true),
                            FALSE = new BoolData(false);


    public BoolData(boolean value){
        this.value = value;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Data clone() {
        return value? TRUE: FALSE;
    }

    @Override
    public String getString() {
        return String.valueOf(value);
    }
}
