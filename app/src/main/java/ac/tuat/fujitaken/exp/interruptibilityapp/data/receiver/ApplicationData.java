package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.DeviceSettings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;

/**
 * 使用中のアプリケーションを取得する
 * Created by hi on 2015/11/24.
 */
public class ApplicationData implements DataReceiver {

    /**
     * バージョンとパーミッションの状態
     */
    private ActivityManager activityManager;
    private PackageManager packageManager;
    private UsageStatsManager statsManager;

    private StringData currentApp = new StringData("");

    ApplicationData(Context context){
        if(!Settings.getDeviceSettings().isUsagePermissionGranted){
            Toast toast = Toast.makeText(context, "使用履歴にアクセスする権限がありません。", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        packageManager = context.getPackageManager();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP){
            activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        else{
            statsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }

    @Override
    public Map<String, Data> getData() {
        Map<String, Data> data = new HashMap<>();
        data.put(APPLICATION, currentApp);
        return data;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String getForegroundApp() {
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - Constants.APP_TIME_LIMITATION;
        String packageName = "";
        List<UsageStats> list = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
        if (list != null && list.size() > 0) {
            SortedMap<Long, UsageStats> map = new TreeMap<>();
            for (UsageStats usageStats : list) {
                map.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!map.isEmpty()) {
                UsageStats stats = map.get(map.lastKey());
                packageName = stats.getPackageName();
            }
        }
        return packageName;
    }

    @SuppressWarnings("deprecation")
    void getCurrentApplication(){

        DeviceSettings settings = Settings.getDeviceSettings();
        if(!settings.isUsagePermissionGranted){
            currentApp.value = "";
            return;
        }

        String value;
        String key;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP){
            key = activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
        else{
            key = getForegroundApp();
        }
        try {
            PackageInfo info = packageManager.getPackageInfo(key, PackageManager.GET_ACTIVITIES);
            CharSequence label = info.applicationInfo.loadLabel(packageManager);
            value = label.toString();
            value = value.replace(",", "，");
            value = value.replace("\n", "，");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            value = "";
        }
        currentApp.value = value;
    }
}
