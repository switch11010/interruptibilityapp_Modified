package ac.tuat.fujitaken.exp.interruptibilityapp.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.RSSI;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.WifiData;

/**
 *
 * Created by hi on 2015/12/01.
 */
public class WifiReceiver extends BroadcastReceiver implements DataReceiver {
    private Context context;
    private WifiManager manager;
    private boolean activate = false;
/*    private Pattern[] patterns;
    private double[] weight;
    private DoubleData dist = new DoubleData(0);
    private List<Map<Integer, Pattern>> buf = new ArrayList<>();*/

    private WifiData wifiData = new WifiData(new ArrayList<RSSI>());

    public WifiReceiver(Context context){
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(this, filter);
        this.context = context;
        manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        activate = mayRequestLocation(context);

/*        List<Spot> list = helper.allSpot();
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
        }*/

        scan();
    }

    public void release(){
        context.unregisterReceiver(this);
    }

    public void scan(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && manager.isScanAlwaysAvailable()) {
            manager.startScan();
        }
        else if (manager.isWifiEnabled()) {
            manager.startScan();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(activate) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
/*            if (flag) {
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
            }*/

                List<ScanResult> results = manager.getScanResults();
                List<RSSI> aps = new ArrayList<>();
                for (ScanResult ap : results) {
                    aps.add(new RSSI(ap.BSSID, ap.frequency, ap.level));
                }
                Collections.sort(aps, new Comparator<RSSI>() {
                    @Override
                    public int compare(RSSI lhs, RSSI rhs) {
                        return rhs.level - lhs.level;
                    }
                });
                wifiData.value = aps;
            }
        }
    }

    private boolean mayRequestLocation(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Toast.makeText(context, "位置情報の権限がありません。", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public Map<String, Data> getData() {
        Map<String, Data> data = new HashMap<>();
        data.put(WIFI, this.wifiData);
        return data;
    }


}
