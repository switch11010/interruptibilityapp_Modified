package ac.tuat.fujitaken.kk.test.testapplication.receiver;

import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ac.tuat.fujitaken.kk.test.testapplication.data.Data;
import ac.tuat.fujitaken.kk.test.testapplication.data.RowData;
import ac.tuat.fujitaken.kk.test.testapplication.loop.RegularThread;

/**
 * 全データを統括するクラス
 * データの最新値をソートしてマップに保存している
 * Created by Komuro on 2015/11/29.
 */
public class AllData implements DataReceiver, RegularThread.ThreadListener {

    //各データを保持しているクラス
    private AccessibilityData accessibilityEventReceiver;
    private ApplicationData applicationInfoReceiver;
    private BroadcastData broadcastEventReceiver;
    private PhoneState phoneState;
    private SensorReceiver sensorReceiver;
    private WalkDetection walkDetection;

    //データ参照用のマップ
    private Map<String, Integer> names = new HashMap<>();
    private Map<String, Data> data;

    //最新値
    private RowData latestLine = new RowData();
    private WifiReceiver wifiReceiver;

    public AllData(Context context, AccelerometerData accelerometerData){
        accessibilityEventReceiver = new AccessibilityData(context);
        applicationInfoReceiver = new ApplicationData(context);
        broadcastEventReceiver = new BroadcastData(context);
        phoneState = new PhoneState(context);
        sensorReceiver = new SensorReceiver(context);
        walkDetection = new WalkDetection(accelerometerData);
        wifiReceiver = new WifiReceiver(context);

        for(int i = 0; i < NAMES.length; i++){
            this.names.put(NAMES[i], i);
        }

        data = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return names.get(lhs).compareTo(names.get(rhs));
            }
        });

        data.putAll(accessibilityEventReceiver.getData());
        data.putAll(applicationInfoReceiver.getData());
        data.putAll(broadcastEventReceiver.getData());
        data.putAll(phoneState.getData());
        data.putAll(sensorReceiver.getData());
        data.putAll(walkDetection.getData());
        data.putAll(wifiReceiver.getData());
    }

    public WalkDetection getWalkDetection(){
        return walkDetection;
    }

    public void put(AccessibilityEvent event){
        accessibilityEventReceiver.put(event);
    }

    public void release(){
        broadcastEventReceiver.release();
        phoneState.release();
        sensorReceiver.release();
    }

    public RowData newLine(){
        latestLine = new RowData();
        for(Data d: data.values()){
            latestLine.data.add(d.clone());
        }
        return latestLine;
    }

    public RowData getLatestLine(){
        return latestLine;
    }

    public String getHeader(){
        StringBuilder header = new StringBuilder();
        for(String name: data.keySet()){
            header.append(name).append(",");
        }
        return header.substring(0, header.length()-1);
    }

    @Override
    public Map<String, Data> getData() {
        return data;
    }

    public void scan(){
        wifiReceiver.scan();
    }

    @Override
    public void run() {
        if(walkDetection.judge()){
            wifiReceiver.scan();
        }
        accessibilityEventReceiver.refresh();
        applicationInfoReceiver.getCurrentApplication();
    }
}
