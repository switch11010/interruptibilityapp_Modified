package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ac.tuat.fujitaken.exp.interruptibilityapp.flow.RegularThread;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;

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
    private WifiReceiver wifiReceiver;

    //データ参照用のマップ
    private Map<String, Integer> names = new HashMap<>();
    private Map<String, Data> data;

    //最新値
    private RowData latestLine = new RowData();

    //s コンストラクタ
    public AllData(Context context, AccelerometerData accelerometerData){
        accessibilityEventReceiver = new AccessibilityData(context);
        applicationInfoReceiver = new ApplicationData(context);
        broadcastEventReceiver = new BroadcastData(context);
        phoneState = new PhoneState(context);
        sensorReceiver = new SensorReceiver(context);
        walkDetection = new WalkDetection(accelerometerData);
        wifiReceiver = new WifiReceiver(context);

        //s NAMES[]：implements元の DataReceiver で宣言してあるString[]型の定数
        //s これを privateな連想配列のキー として設定
        //s names["APPLICATION"] = 0;
        for(int i = 0; i < NAMES.length; i++){
            this.names.put(NAMES[i], i);
        }

        data = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                Integer left = names.get(lhs);
                return left.compareTo(names.get(rhs));
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

    //s MainService の onAccessibilityEvent() で呼ばれる
    public void put(AccessibilityEvent event){
        accessibilityEventReceiver.put(event);
    }

    public void release(){
        broadcastEventReceiver.release();
        phoneState.release();
        sensorReceiver.release();
    }

    @SuppressWarnings("Convert2streamapi")
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
            header.append(name);
            header.append(",");
        }
        return header.substring(0, header.length()-1);
    }

    @Override
    //s クラス DataReceiver からの implements
    public Map<String, Data> getData() {
        return data;
    }

    public void scan(){
        wifiReceiver.scan();
    }

    @Override
    //s クラス RegularThread からの implements
    //s 定期実行される
    public void run() {
        if(walkDetection.isWalkingNext()){
            wifiReceiver.scan();
        }
        accessibilityEventReceiver.refresh();
        applicationInfoReceiver.getCurrentApplication();
    }
}
