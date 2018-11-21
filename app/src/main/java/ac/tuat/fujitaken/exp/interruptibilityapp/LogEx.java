package ac.tuat.fujitaken.exp.interruptibilityapp;

import android.util.Log;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;

/**
 * s 通常の Log を使ってログ出力をしつつ、ファイル出力も行う（通常の Log の代わりに使うことを想定）
 * 作成：2018/11/12
 */
public class LogEx {
    private static SaveData saveData = null;

    private static final int VERBOSE = 2;
    private static final int DEBUG = 3;
    private static final int INFO = 4;
    private static final int WARN = 5;
    private static final int ERROR = 6;

    static {
        //saveData = new SaveData("LogEx", "Tag,Msg");
    }

    //s ログの SaveData への記録を開始する（既に開始済みなら特に何もしない）
    //s Settings.settings(), MainService.onCreate() から呼ばれる
    public synchronized static SaveData start() {
        if (saveData == null) {
            LogEx.d("LogEx.start()", "initialized");
            saveData = new SaveData("LogEx", "Lv,Tag,Msg");
        }
        LogEx.d("LogEx.start()", "started");
        return saveData;
    }

    //s ゲッタ（SaveTaskへの登録に使用 を想定）
    public static SaveData getSaveData() {
        return saveData;
    }

    //s ログの SaveData への出力を停止する
    //s MainService.onDestroy() から呼ばれる
    public static void stop() {
        LogEx.d("LogEx.stop()", "stop logging");
        saveData = null;
    }


    //s ログファイルに記録する（と見せかけて実際は SaveData 内に保持 ＆ 3分おきに SaveTask が保存する）
    private synchronized static void printLog(int level, String tag, String msg) {
        if (saveData == null) {
            //Log.e("LogEx.printLog", "saveData が null");
            return;
        }

        String[] levelToString = { "V", "D", "I", "W", "E" };
        RowData latestLine = new RowData();
        latestLine.data.add(new StringData(levelToString[level - 2]));
        latestLine.data.add(new StringData(tag));
        latestLine.data.add(new StringData(msg));
        saveData.addLine(latestLine);
    }


    public static int v(String tag, String msg) {
        new Thread( ()->printLog(VERBOSE, tag, msg) ).start();
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(VERBOSE, tag, msg) ).start();
        return Log.v(tag, msg, tr);
    }


    public static int d(String tag, String msg) {
        new Thread( ()->printLog(DEBUG, tag, msg) ).start();
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(DEBUG, tag, msg) ).start();
        return Log.d(tag, msg, tr);
    }


    public static int i(String tag, String msg) {
        new Thread( ()->printLog(INFO, tag, msg) ).start();
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(INFO, tag, msg) ).start();
        return Log.i(tag, msg, tr);
    }


    public static int w(String tag, String msg) {
        new Thread( ()->printLog(WARN, tag, msg) ).start();
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(WARN, tag, msg) ).start();
        return Log.w(tag, msg, tr);
    }


    public static int e(String tag, String msg) {
        new Thread( ()->printLog(ERROR, tag, msg) ).start();
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        new Thread( ()->printLog(ERROR, tag, msg) ).start();
        return Log.e(tag, msg, tr);
    }

}
