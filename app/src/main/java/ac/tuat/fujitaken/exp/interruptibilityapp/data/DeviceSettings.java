package ac.tuat.fujitaken.exp.interruptibilityapp.data;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Process;
import android.support.annotation.RequiresApi;

/**
 *
 * Created by hi on 2017/02/07.
 */
@SuppressWarnings("UnusedDeclaration")
public class DeviceSettings {

    @SuppressWarnings("WeakerAccess")
    public static final int ABOVE_6 = 1, UNDER_6 = 2;

    @SuppressWarnings("WeakerAccess")
    public final int VERSION;
    @SuppressWarnings("WeakerAccess")
    public final boolean isUsagePermissionGranted,
            isStoragePermissionGranted,
            isPhonePermissionGranted,
            isLocationPermissionGranted;
    private WifiManager manager;

    public DeviceSettings(Context context){
        manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            VERSION = ABOVE_6;
            isUsagePermissionGranted = context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED;
            isStoragePermissionGranted = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            isPhonePermissionGranted = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            isLocationPermissionGranted = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        else{
            VERSION = UNDER_6;
            isStoragePermissionGranted = true;
            isPhonePermissionGranted = true;
            isLocationPermissionGranted = true;

            boolean usagePermission = true;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                usagePermission = checkPermission(context);
            }
            isUsagePermissionGranted = usagePermission;
        }
    }

    public boolean isWifiEnabled(){
        return manager.isWifiEnabled();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static boolean checkPermission(Context context) {

        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOpsManager.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
                context.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return context.checkPermission("android.permission.PACKAGE_USAGE_STATS",
                    Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
        }

        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
