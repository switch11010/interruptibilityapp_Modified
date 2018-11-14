package ac.tuat.fujitaken.exp.interruptibilityapp;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;

/**
 * s 通常の Log を使ってログ出力をしつつ、ファイル出力も行う（通常の Log の代わりに使うことを想定）
 * 作成：2018/11/12
 */
public class Log {
    private static final SaveData saveData = new SaveData("Log", "Level,Tag,Msg");

    private static final int VERBOSE = 2;
    private static final int DEBUG = 3;
    private static final int INFO = 4;
    private static final int WARN = 5;
    private static final int ERROR = 6;

    static {
        Log.d("Log", "new SaveData() が完了");
        //saveData = new SaveData("Log", "Tag,Msg");
    }

    public static SaveData getSaveData() {
        return saveData;
    }


    //s ログファイルに記録する（と見せかけて実際は SaveData 内に保持 ＆ 3分おきに SaveTask が保存する）
    private synchronized static void printLog(int level, String tag, String msg) {
        String[] levelToString = { "V", "D", "I", "W", "E" };
        RowData latestLine = new RowData();
        latestLine.data.add(new StringData(levelToString[level - 2]));
        latestLine.data.add(new StringData(tag));
        latestLine.data.add(new StringData(msg));
        saveData.addLine(latestLine);
    }


    public static int v(String tag, String msg) {
        new Thread( ()->printLog(VERBOSE, tag, msg) ).start();
        return android.util.Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(VERBOSE, tag, msg) ).start();
        return android.util.Log.v(tag, msg, tr);
    }


    public static int d(String tag, String msg) {
        new Thread( ()->printLog(DEBUG, tag, msg) ).start();
        return android.util.Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(DEBUG, tag, msg) ).start();
        return android.util.Log.d(tag, msg, tr);
    }


    public static int i(String tag, String msg) {
        new Thread( ()->printLog(INFO, tag, msg) ).start();
        return android.util.Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(INFO, tag, msg) ).start();
        return android.util.Log.i(tag, msg, tr);
    }


    public static int w(String tag, String msg) {
        new Thread( ()->printLog(WARN, tag, msg) ).start();
        return android.util.Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(WARN, tag, msg) ).start();
        return android.util.Log.w(tag, msg, tr);
    }


    public static int e(String tag, String msg) {
        new Thread( ()->printLog(ERROR, tag, msg) ).start();
        return android.util.Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(ERROR, tag, msg) ).start();
        return android.util.Log.e(tag, msg, tr);
    }

}
