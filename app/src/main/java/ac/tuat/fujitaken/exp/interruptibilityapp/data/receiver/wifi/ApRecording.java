package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data.AccessPoint;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data.ListItem;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data.Pattern;


/**
 *
 * Created by seuo on 15/06/30.
 */
public class ApRecording implements WifiWatching.ScanEventAction{
    private static final double RECORDING_RATIO_THRESHOLD = 0.8;

    private Map<AccessPoint, Pattern> record;
    private Map<AccessPoint, WifiCounter> counter;
    private WifiWatching watching;
    private WifiWatching.ScanEventAction action;
    private int count = 0;

    public ApRecording(Context context){
        this(context, new HashMap<>());
    }

    public ApRecording(Context context, Map<AccessPoint, Pattern> patternMap){
        counter = new HashMap<>();
        this.record = patternMap;
        watching = new WifiWatching(context);
    }

    private List<ListItem> update(List<ScanResult> results){
        for(ScanResult s: results) {
            AccessPoint ap = new AccessPoint(s.BSSID, s.SSID, s.frequency);
            Pattern p = record.get(ap);
            WifiCounter i = counter.get(ap);
            if (p == null) {
                record.put(ap, new Pattern(-1, -1, s.level, 1));
            } else {
                p.averageLevel = (p.averageLevel * p.sampleCount + s.level) / (p.sampleCount + 1);
                p.sampleCount++;
            }
            if(i == null){
                counter.put(ap, new WifiCounter(s.level));
            }
            else{
                i.setVar(s.level);
            }
        }
        count++;
        return toList();
    }

    public Map<AccessPoint, Pattern> getRecordMap(){
        for (AccessPoint ap: counter.keySet()){
            WifiCounter c = counter.get(ap);
            if(c.getCount() <= count * RECORDING_RATIO_THRESHOLD){
                record.remove(ap);
            }
        }
        return record;
    }

    public List<ListItem> toList(){
        List<ListItem> list = new ArrayList<>();
        //noinspection Convert2streamapi
        for(Map.Entry<AccessPoint, Pattern> pattern: record.entrySet()){
            list.add(new ListItem(pattern.getKey(), pattern.getValue()));
        }
        Collections.sort(list, (t1, t2) -> Double.compare(t2.pattern.averageLevel, t1.pattern.averageLevel));
        return list;
    }

    public void start(){
        count = 0;
        counter = new HashMap<>();
        watching.setScanResultAvailableAction(this);
        watching.start();
    }

    public void stop(){
        watching.removeAction(this);
        watching.stop();
    }

    public void setAction(WifiWatching.ScanEventAction eventAction){
        this.action = eventAction;
    }

    @Override
    public void onScanResultAvailable() {
        update(watching.getScanList());
        if(action != null){
            action.onScanResultAvailable();
        }
    }
}
