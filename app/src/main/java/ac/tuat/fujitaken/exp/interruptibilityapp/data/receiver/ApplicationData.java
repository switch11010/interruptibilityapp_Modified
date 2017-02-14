package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.*;
import android.os.Process;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;

/**
 * 使用中のアプリケーションを取得する
 * Created by hi on 2015/11/24.
 */
public class ApplicationData implements DataReceiver {

    /**
     * バージョンとパーミッションの状態
     */
    private static final int OLD = 1,   //キットカットより古いバージョン
            NOT_HAVE_PERMISSION = 2,     //キットカット以上でパーミッションなし
            HAVE_PERMISSION = 3;          //キットカット以上でパーミッションあり

    private ActivityManager activityManager;
    private PackageManager packageManager;
    private UsageStatsManager statsManager;
    private int version = 0;

    private StringData currentApp = new StringData("");

    ApplicationData(Context context){

        packageManager = context.getPackageManager();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP){
            activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            version = OLD;
        }
        else if(checkPermission(context)){
            statsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
           version = HAVE_PERMISSION;
        }
        else{
            version = NOT_HAVE_PERMISSION;
        }
        if(version == NOT_HAVE_PERMISSION){
            Toast toast = Toast.makeText(context, "使用履歴にアクセスする権限がありません。", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public Map<String, Data> getData() {
        Map<String, Data> data = new HashMap<>();
        data.put(APPLICATION, currentApp);
        return data;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean checkPermission(Context context) {

        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOpsManager.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
                context.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return context.checkPermission("android.permission.PACKAGE_USAGE_STATS",
                    Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
        }

        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String getForegroundApp() {
        if(version != HAVE_PERMISSION){
            return "";
        }

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
        String value;
        String key;
        switch (version){
            case OLD:
                List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
                ActivityManager.RunningTaskInfo info = tasks.get(0);
                key = info.topActivity.getPackageName();
                break;
            case HAVE_PERMISSION:
                key = getForegroundApp();
                break;
            default:
                key = "";
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
