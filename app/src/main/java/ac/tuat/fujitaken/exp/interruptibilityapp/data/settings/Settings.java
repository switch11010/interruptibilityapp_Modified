package ac.tuat.fujitaken.exp.interruptibilityapp.data.settings;

import android.content.Context;

/**
 *
 * Created by hi on 2017/02/28.
 */


public class Settings {
    private static Settings instance;
    private MyApplication myApp;
    private AppSettings appSettings;
    private DeviceSettings deviceSettings;
    private EventCounter eventCounter;

    static void createInstance(MyApplication app){
        instance = new Settings(app);
    }

    private Settings(MyApplication app){
        this.myApp = app;
        appSettings = new AppSettings();
        deviceSettings = new DeviceSettings(myApp.getApplicationContext());
        eventCounter = new EventCounter(myApp.getApplicationContext());
    }

    public static AppSettings getAppSettings() {
        return instance.appSettings;
    }

    public static Context getContext() {
        return instance.myApp.getApplicationContext();
    }

    public static DeviceSettings getDeviceSettings() {
        return instance.deviceSettings;
    }

    public static EventCounter getEventCounter() {
        return instance.eventCounter;
    }
}
