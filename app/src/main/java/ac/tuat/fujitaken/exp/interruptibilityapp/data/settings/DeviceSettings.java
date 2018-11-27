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
            //s 変更ここから：ログ出力を追加＆判定式を分離
            int perm1, perm2;

            perm1 = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            perm2 = PackageManager.PERMISSION_GRANTED;
            LogEx.d("DeviceSettings()", "isStoragePermissionGranted: " + perm1 + " == " + perm2 + " -> " + (perm1 == perm2));
            isStoragePermissionGranted = (perm1 == perm2);

            perm1 = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
            perm2 = PackageManager.PERMISSION_GRANTED;
            LogEx.d("DeviceSettings()", "isPhonePermissionGranted: " + perm1 + " == " + perm2 + " -> " + (perm1 == perm2));
            isPhonePermissionGranted = (perm1 == perm2);

            perm1 = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            perm2 = PackageManager.PERMISSION_GRANTED;
            LogEx.d("DeviceSettings()", "isLocationPermissionGranted: " + perm1 + " == " + perm2 + " -> " + (perm1 == perm2));
            isLocationPermissionGranted = (perm1 == perm2);
            //s 変更ここまで
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
            LogEx.e("DSettings.checkPerm()", "appOpsManager == null");  //s 追加
            return false;
        }
        int mode = appOpsManager.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
                context.getPackageName());
        LogEx.d("DSettings.checkPerm()", "appOpsManager.checkOp(): " + mode);  //s 追加
        LogEx.d("DSettings.checkPerm()", "AppOpsManager.MODE_DEFAULT: " + AppOpsManager.MODE_DEFAULT);  //s 追加
        LogEx.d("DSettings.checkPerm()", "AppOpsManager.MODE_ALLOWED: " + AppOpsManager.MODE_ALLOWED);  //s 追加
        if (mode == AppOpsManager.MODE_DEFAULT) {
            int permissionState = context.checkPermission("android.permission.PACKAGE_USAGE_STATS", Process.myPid(), Process.myUid());  //s 追加：return から分割
            int granted = PackageManager.PERMISSION_GRANTED;  //s 追加：return から分割
            LogEx.d("DSettings.checkPerm()", "context.checkPermission(): " + permissionState);  //s 追加
            LogEx.d("DSettings.checkPerm()", "PackageManager.PERMISSION_GRANTED: " + granted);  //s 追加
            return permissionState == granted;  //s 変更：↑に分割
        }

        LogEx.d("DSettings.checkPerm()", "checkOp() == MODE_ALLOWED: " + (mode == AppOpsManager.MODE_ALLOWED));  //s 追加
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public WifiManager getManager() {
        return manager;
    }
}
