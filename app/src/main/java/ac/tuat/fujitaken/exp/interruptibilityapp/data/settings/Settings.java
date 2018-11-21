package ac.tuat.fujitaken.exp.interruptibilityapp.data.settings;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;  //s 自作Log


/**
 *
 * Created by hi on 2017/02/28.
 */

public class Settings extends Application {

    private static Settings instance;
    private AppSettings appSettings;
    private DeviceSettings deviceSettings;
    private EventCounter eventCounter;

    @Override
    public void onCreate() {
        Log.d("Info", "Settings.onCreate_1");  //s 追加（自作Logでは↓でのインスタンス生成が必要なので普通Log）
        super.onCreate();
        Log.d("Info", "Settings.onCreate_2");  //s 追加（同上）

        instance = this;
        instance.settings();
        LogEx.d("Info", "Settings.onCreate_3e");  //s 追加
    }

    private void settings(){
        appSettings = new AppSettings(getApplicationContext());
        LogEx.start();  //s 追加：ログの SaveData への記録を開始（↑の appSettings が必要）
        deviceSettings = new DeviceSettings(getApplicationContext());
        eventCounter = new EventCounter(getApplicationContext());
    }

    public static AppSettings getAppSettings() {
        return instance.appSettings;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static DeviceSettings getDeviceSettings() {
        return instance.deviceSettings;
    }

    public static EventCounter getEventCounter() {
        return instance.eventCounter;
    }
}
