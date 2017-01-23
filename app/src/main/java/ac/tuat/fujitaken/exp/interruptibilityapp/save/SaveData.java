package ac.tuat.fujitaken.exp.interruptibilityapp.save;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.RowData;

/**
 * データを一定時間で保存するクラス
 * Created by hi on 2015/11/09.
 */
public class SaveData{

    private File file;      //保存ファイル
    protected List<RowData> data = new ArrayList<>();    //保存するデータ
    private String header, category;
    public boolean rock = false;

    /**
     * コンストラクタ
     * @param category  ファイルのカテゴリー
     */
    public SaveData(String category, String header) {
        this.header = header;
        this.category = category;
        updateFile();
    }

    public void updateFile(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
        String fileName = sdf.format(System.currentTimeMillis()) + "_" + category;
        String fileName1 = Environment.getExternalStorageDirectory().getPath() + "/EventLog/" + fileName;
        file = new File(fileName1 + ".csv");
    }

    public File getFile() {
        return file;
    }

    public String getHeader() {
        return "time," + header;
    }

    /**
     * データ行の追加
     * @param newLine   新しい行
     */
    public void addLine(RowData newLine){
        if(data != null) {
            data.add(newLine);
        }
    }

    public List<RowData> refresh(){
        List<RowData> temp = data;
        data = new ArrayList<>();
        return temp;
    }
}
