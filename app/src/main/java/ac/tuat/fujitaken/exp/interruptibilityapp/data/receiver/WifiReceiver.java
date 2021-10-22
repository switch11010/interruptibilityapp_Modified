package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.RSSI;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.WifiData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.DeviceSettings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;

/**
 *
 * Created by hi on 2015/12/01.
 */
public class WifiReceiver extends BroadcastReceiver implements DataReceiver {
    private boolean activate = false;
    private Context context;
    /*
    private Pattern[] patterns;
    private double[] weight;
    private DoubleData dist = new DoubleData(0);
    private List<Map<Integer, Pattern>> buf = new ArrayList<>();
    */

    private WifiData wifiData = new WifiData(new ArrayList<>());

    public WifiReceiver(Context c){
        IntentFilter scanFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        IntentFilter wifiChangeFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        c.registerReceiver(this, scanFilter);
        c.registerReceiver(this, wifiChangeFilter);
        activate = hasRequestLocation(c);
        this.context = c;
        /*
        List<Spot> list = helper.allSpot();
        flag = list.size() > 0;
        if(flag) {
            SQLiteDatabase db = helper.getReadableDatabase();
            HashMap<AccessPoint, Pattern> patterns = helper.selectPatternFromSpotID(db, list.get(0).id);
            this.patterns = patterns.values().toArray(new Pattern[patterns.size()]);
            db.close();

            weight = new double[this.patterns.length];
            double s = 0;
            for(int i = 0; i < weight.length; i++){
                Pattern p = this.patterns[i];
                weight[i] = translate(p.averageLevel);
                s += weight[i];
            }
            for(int i = 0; i < weight.length; i++){
                weight[i] /= s;
            }
        }
        */

        scan();
    }

    public void release(){
        context.unregisterReceiver(this);
    }

    public void scan(){
        DeviceSettings settings = Settings.getDeviceSettings();
        WifiManager manager = settings.getManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && manager.isScanAlwaysAvailable()) {
            manager.startScan();
        }
        else if (manager.isWifiEnabled()) {
            manager.startScan();
        }
    }

    public static void sendIP(Context context){
        if(!isActiveWifi(context)){
            return;
        }

        DeviceSettings settings = Settings.getDeviceSettings();
        WifiManager manager = settings.getManager();
        WifiInfo wINfo = manager.getConnectionInfo();
        int ipAdr = wINfo.getIpAddress();
        //UDPConnection.sendIP(ipAdr);
    }

    public void sendIP(){
        sendIP(context);
    }

    private static boolean isActiveWifi(Context context){
        DeviceSettings settings = Settings.getDeviceSettings();
        if(!settings.isWifiEnabled()){
            return false;
        }
        // Activity 等の Context 内で
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null){// && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            // シンプルな状態を取得
            NetworkInfo.State networkState = networkInfo.getState();
            if(networkState == NetworkInfo.State.CONNECTED){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        String action = intent.getAction();
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            switch (info.getState()) {
                case DISCONNECTED:
                    break;
                case SUSPENDED:
                    break;
                case CONNECTING:
                    break;
                case CONNECTED:
                    /**
                     * 自身のIPを送信する処理
                     */
                    sendIP();
                    break;
                case DISCONNECTING:
                    break;
                case UNKNOWN:
                    break;
                default:
                    break;
            }
        }
        else if(activate) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                /*
                if (flag) {
                List<ScanResult> results = manager.getScanResults();
                Map<AccessPoint, Pattern> record = new HashMap<>();

                for (ScanResult s : results) {
                    record.put(new AccessPoint(s.BSSID, s.SSID, s.frequency),
                            new Pattern(-1, -1, s.level, 1));
                }

                SQLiteDatabase db = helper.getReadableDatabase();
                helper.lookupAndFillPatternApIDs(db, record);

                //メンバのマップの作成
                Map<Integer, Pattern> herePatternMap = new HashMap<>();

                //引数のマップをapidで参照できるように変換
                for (Pattern pattern : record.values())
                    herePatternMap.put(pattern.apid, pattern);

                db.close();

                buf.add(herePatternMap);
                if (buf.size() > 3) {
                    buf.remove(0);
                }

                if (patterns.length > 3) {
                    double sum = 0;
                    int size = 0;

                    //DBから取り出したパターンと現在位置のパターンを比較する
                    for (int i = 0; i < patterns.length; i++) {

                        Pattern tempDBPattern = patterns[i];
                        Pattern tempHerePattern;
                        double t = 0;
                        int s = 0;
                        for (Map<Integer, Pattern> map : buf) {
                            tempHerePattern = map.get(tempDBPattern.apid);
                            if (tempHerePattern != null) {
                                t += tempHerePattern.averageLevel;
                                s++;
                            }
                        }

                        if (s > 0) {
                            t /= s;
                            double exp = calc(Math.abs(tempDBPattern.averageLevel - t));
                            sum += weight[i] * exp;
                            size++;
                        }
                    }

                    if (size > 3) {
                        dist.value = sum;
                    }
                }
            }
            */

                DeviceSettings settings = Settings.getDeviceSettings();
                WifiManager manager = settings.getManager();

                List<ScanResult> results = manager.getScanResults();
                List<RSSI> aps = new ArrayList<>();
                //noinspection Convert2streamapi
                for (ScanResult ap : results) {
                    aps.add(new RSSI(ap.BSSID, ap.frequency, ap.level));
                }
                Collections.sort(aps, (lhs, rhs) -> rhs.level - lhs.level);
                wifiData.value = aps;
            }
        }
    }

    private boolean hasRequestLocation(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Toast toast = Toast.makeText(c, "位置情報の権限がありません。", Toast.LENGTH_SHORT);
        toast.show();
        return false;
    }

    @Override
    public Map<String, Data> getData() {
        Map<String, Data> data = new HashMap<>();
        data.put(WIFI, this.wifiData);
        return data;
    }


}
