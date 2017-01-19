package ac.tuat.fujitaken.kk.test.testapplication.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 1行分の記録データ
 * 生成時に時間を取得する
 * Created by hi on 2015/11/10.
 */
public class RowData implements Cloneable, Serializable{
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.JAPAN);
    public List<Data> data;
    public long time;

    public RowData(){
        this.time = System.currentTimeMillis();
        this.data = new ArrayList<>();
    }

    public String getLine(){
        StringBuilder builder = new StringBuilder();
        builder.append(DATE_FORMAT.format(time))
                .append(",");
        for(Data d: data){
            builder.append(d.getString())
                    .append(",");
        }
        return builder.substring(0, builder.length()-1);
    }

    @Override
    public RowData clone(){
        RowData copy = null;
        try {
            copy = (RowData)super.clone();
            copy.data = data;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return copy;
    }
}
