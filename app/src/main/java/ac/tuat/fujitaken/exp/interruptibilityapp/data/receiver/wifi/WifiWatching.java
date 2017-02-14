package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * マッチング用
 * Created by seuo on 15/06/30.
 */
public class WifiWatching{

    private volatile boolean isReceiverRegistered = false;
    private Context context;
    public WifiManager manager;
    private IntentFilter filter1 = new IntentFilter("android.net.wifi.SCAN_RESULTS");
    private List<ScanResult> results = new ArrayList<>();
    private List<ScanEventAction> actions = new ArrayList<>();

    private ScheduledExecutorService service;

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            manager.startScan();
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                results = manager.getScanResults();

                for (int i = 0; i < actions.size(); i++) {
                    ScanEventAction action = actions.get(i);
                    if (action == null) {
                        actions.remove(i);
                        i--;
                    } else {
                        action.onScanResultAvailable();
                    }
                }
            }
        }
    };

    void setScanResultAvailableAction(ScanEventAction action){
        actions.add(action);
    }

    WifiWatching(Context c){
        this.context = c;
        manager = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
    }

    public void start(){
        if(!isReceiverRegistered) {
            context.registerReceiver(receiver, filter1);
            isReceiverRegistered = true;
            service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        }
    }

    void stop(){
        if(isReceiverRegistered) {
            context.unregisterReceiver(receiver);
            service.shutdown();
            isReceiverRegistered = false;
        }
    }

    void removeAction(ScanEventAction action){
        for(int i = 0; i < actions.size(); i++) {
            if(action.equals(actions.get(i))){
                actions.remove(i);
                return;
            }
        }
    }

    List<ScanResult> getScanList(){
        return results;
    }

    public interface ScanEventAction{
        void onScanResultAvailable();
    }
}
