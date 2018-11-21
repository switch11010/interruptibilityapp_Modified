package ac.tuat.fujitaken.exp.interruptibilityapp.data.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Process;
import android.support.annotation.RequiresApi;
import android.util.Log;

import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;  //s 自作Log

/**
 *
 * Created by hi on 2017/02/07.
 */
@SuppressWarnings("UnusedDeclaration")
public class DeviceSettings {

    @SuppressWarnings("WeakerAccess")
    public final boolean isUsagePermissionGranted,
            isStoragePermissionGranted,
            isPhonePermissionGranted,
            isLocationPermissionGranted;
    private WifiManager manager;

    @SuppressLint("WifiManagerPotentialLeak")
    DeviceSettings(Context context){
        manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isStoragePermissionGranted = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            isPhonePermissionGranted = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            isLocationPermissionGranted = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        else{
            isStoragePermissionGranted = true;
            isPhonePermissionGranted = true;
            isLocationPermissionGranted = true;
        }

        boolean usagePermission = true;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usagePermission = checkPermission(context);
        }
        isUsagePermissionGranted = usagePermission;

        LogEx.d("Permission", isUsagePermissionGranted + ", "
                + isStoragePermissionGranted + ","
                + isPhonePermissionGranted + ","
                + isLocationPermissionGranted);
    }

    public boolean isWifiEnabled(){
        return manager.isWifiEnabled();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static boolean checkPermission(Context context) {

        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        if(appOpsManager == null){
            return false;
        }
        int mode = appOpsManager.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
                context.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return context.checkPermission("android.permission.PACKAGE_USAGE_STATS",
                    Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
        }

        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public WifiManager getManager() {
        return manager;
    }
}
