package ac.tuat.fujitaken.exp.interruptibilityapp.data.base;

/**
 * 
 * Created by hi on 2015/11/18.
 */
public class StringData extends Data {
    public String value;
    private static final StringData BLANK = new StringData("");

    public StringData(String value){
        this.value = value;
    }

    @Override
    public Data clone() {
        if("".equals(value)){
            return BLANK;
        }
        else {
            return super.clone();
        }
    }

    @Override
    public String getString() {
        return value;
    }
}
