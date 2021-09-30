package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;  //s 自作Log
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.DeviceSettings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;

import static android.content.ContentValues.TAG;
import static android.content.Context.ACTIVITY_SERVICE;
import java.util.Calendar;
import java.util.stream.Collectors;

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
    private StringData diffApp = new StringData("");

    private List<UsageStats> list = new ArrayList<UsageStats>();
    private List<UsageStats> prevlist = new ArrayList<UsageStats>();
    private String prevPackegeName = "";

    private Context context;

    ApplicationData(Context context){
        if(!Settings.getDeviceSettings().isUsagePermissionGranted){
            LogEx.e("ApplicationData", "Permission が許可されていない（使用履歴にアクセスできるアプリ）");  //s 追加
            Toast toast = Toast.makeText(context, "使用履歴にアクセスする権限がありません。", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        packageManager = context.getPackageManager();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP){
            activityManager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getForegroundApp() {
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - Constants.APP_TIME_LIMITATION;
        String packageName = "";
        list = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
        if (list != null && list.size() > 0) {
            SortedMap<Long, UsageStats> map = new TreeMap<>();
            for (UsageStats usageStats : list) {
                map.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!map.isEmpty()) {
                UsageStats stats = map.get(map.lastKey());
                packageName = stats.getPackageName();
            }
//            if(!packageName.equals(prevPackegeName)) {
//                for (UsageStats usageStats : list) {
//                    if (usageStats.getTotalTimeInForeground() > 0) {
//                        Log.d(TAG, "packageName: " + usageStats.getPackageName() + "\ttotalTimeDisplayed: " + usageStats.getTotalTimeInForeground()
//                                + "\tfirstTime: " + getStringDate(usageStats.getFirstTimeStamp()) + "\tlastTime: " + getStringDate(usageStats.getLastTimeUsed()));
//                    }
//                }
//            }
        }
       
        prevlist=list;
        //ここまで
        prevPackegeName = packageName;

        return packageName;
    }
    private String getStringDate(long milliseconds) {
        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE);
        final Date date = new Date(milliseconds);
        return df.format(date);
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
//            int test = packageManager.getPackageUid(key, PackageManager.GET_ACTIVITIES);
//            Log.d(TAG, String.valueOf(test));
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
