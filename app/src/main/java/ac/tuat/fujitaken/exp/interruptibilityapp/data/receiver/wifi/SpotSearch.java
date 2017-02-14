package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data.AccessPoint;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data.Pattern;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data.SpotAndDist;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.database.WiPSDBHelper;

/**
 *
 * Created by seuo on 15/06/30.
 */
public class SpotSearch implements WifiWatching.ScanEventAction {
    public WifiWatching watching;
    private WiPSDBHelper helper;
    private WifiWatching.ScanEventAction action;

    private HashMap<Integer, Pattern[]> dbPatternArrayMap;
    private HashMap<Integer, Pattern> herePatternMap;
    private List<Integer> spotIDList;
    private List<Map<Integer, Pattern>> buf = new ArrayList<>();
    private Map<Integer, Map<Integer, Pattern>> dbMap;

    public volatile boolean searching = false;

    public SpotSearch(Context context, WiPSDBHelper wiPSDBHelper){
        this.helper = wiPSDBHelper;
        watching = new WifiWatching(context);
    }

    public void start(){
        searching = true;
        watching.setScanResultAvailableAction(this);
        watching.start();
    }

    public void stop(){
        watching.removeAction(this);
        watching.stop();
        searching = false;
    }

    public void setAction(WifiWatching.ScanEventAction eventAction){
        this.action = eventAction;
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onScanResultAvailable() {
        Map<AccessPoint, Pattern> record = new HashMap<>();

        for(ScanResult s: watching.getScanList()){
            record.put(new AccessPoint(s.BSSID, s.SSID, s.frequency),
                    new Pattern(-1, -1, s.level, 1));
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        helper.lookupAndFillPatternApIDs(db, record);

        //メンバのマップの作成
        dbPatternArrayMap = new HashMap<>();
        herePatternMap = new HashMap<>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") HashMap<Integer, Integer> apMap = new HashMap<>();
        dbMap = new HashMap<>();

        //引数のマップをapidで参照できるように変換
        for (Pattern pattern : record.values()) {
            herePatternMap.put(pattern.apid, pattern);
        }

        //対応するapをもつspotidを取り出す
        Collection<Pattern> spots = herePatternMap.values();
        spotIDList = helper.selectSpotIDFromApID(db, spots.toArray(new Pattern[herePatternMap.size()]));

        //dbから対応するspotidのパターンを取り出す
        for (int n = 0; n < spotIDList.size(); n++) {
            HashMap<AccessPoint, Pattern> patterns = helper.selectPatternFromSpotID(db, spotIDList.get(n));

            Map<Integer, Pattern> temp = new HashMap<>();
            for(Map.Entry<AccessPoint, Pattern> entry: patterns.entrySet()){
                temp.put(entry.getKey().id, entry.getValue());
            }

            dbMap.put(spotIDList.get(n), temp);

            for(AccessPoint ap: patterns.keySet()){
                apMap.put(ap.id, ap.frequency);
            }

            //パターンをマップに格納する
            Collection<Pattern> c = patterns.values();
            dbPatternArrayMap.put(spotIDList.get(n), c.toArray(new Pattern[patterns.size()]));
            //電波の強さでソート
            Arrays.sort(dbPatternArrayMap.get(spotIDList.get(n)), (lhs, rhs) -> -Double.compare(lhs.averageLevel, rhs.averageLevel));
        }
        buf.add(herePatternMap);
        if(buf.size() > 3){
            buf.remove(0);
        }

        db.close();

        if (action != null) {
            action.onScanResultAvailable();
        }
    }

    public List<SpotAndDist> matching(){
        //結果を保存するためのリスト
        List<SpotAndDist> resultSpotList = new ArrayList<>();

        //距離を計算する
        for (Integer spotID: spotIDList) {

            Map<Integer, Pattern> tempDB = dbMap.get(spotID);
            HashSet<Integer> keys = new HashSet<>();
            keys.addAll(tempDB.keySet());
            keys.addAll(herePatternMap.keySet());

            double numerator = 0,
                    denominator = 0;

            for (Integer key : keys) {
                Pattern a = tempDB.get(key);
                Pattern b = herePatternMap.get(key);
                if (a == null) {
                    numerator += 0;
                    denominator += b.averageLevel;
                } else if (b == null) {
                    numerator += 0;
                    denominator += a.averageLevel;
                } else {
                    numerator += Math.max(a.averageLevel, b.averageLevel);
                    denominator += Math.min(a.averageLevel, b.averageLevel);
                }
            }
            if (denominator != 0) {
                resultSpotList.add(new SpotAndDist(numerator / denominator, spotID));
            }
        }

        //リストをソート
        Collections.sort(resultSpotList, (lhs, rhs) -> Double.compare(rhs.getDist(), lhs.getDist()));

        return resultSpotList;
    }

    public List<SpotAndDist> matchingtest(){
        //結果を保存するためのリスト
        List<SpotAndDist> resultSpotList = new ArrayList<>();

        //距離を計算する
        for (Integer spotID: spotIDList) {

            Map<Integer, Pattern> tempDB = dbMap.get(spotID);
            HashSet<Integer> keys = new HashSet<>();
            keys.addAll(tempDB.keySet());
            keys.addAll(herePatternMap.keySet());

            double numerator = 0,
                    denominator = 0;

            for (Integer key : keys) {
                Pattern a = tempDB.get(key);
                Pattern b = herePatternMap.get(key);
                if (a == null) {
                    numerator += 0;
                    denominator += b.averageLevel;
                } else if (b == null) {
                    numerator += 0;
                    denominator += a.averageLevel;
                } else {
                    numerator += Math.max(a.averageLevel, b.averageLevel);
                    denominator += a.averageLevel + b.averageLevel;
                }
            }
            if (denominator != 0) {
                resultSpotList.add(new SpotAndDist(2 * numerator / denominator, spotID));
            }
        }

        //リストをソート
        Collections.sort(resultSpotList, (lhs, rhs) -> Double.compare(rhs.getDist(), lhs.getDist()));

        return resultSpotList;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<SpotAndDist> matching2(){
        //結果を保存するためのリスト
        List<SpotAndDist> resultSpotList = new ArrayList<>();

        if(herePatternMap.size() < 5) {
            return resultSpotList;
        }

        //距離を計算する
        for (int n = 0; n < spotIDList.size(); n++) {

            //DBのパターンの配列
            Pattern[] dbPattern = dbPatternArrayMap.get(spotIDList.get(n));

            if(dbPattern.length < 5) {
                continue;
            }

            double distance = 0;
            int size = 0;

            //DBから取り出したパターンと現在位置のパターンを比較する
            for (Pattern tempDBPattern : dbPattern) {

                //一時的な変数
                Pattern tempHerePattern;
                double t = 0;
                int i = 0;
                for (Map<Integer, Pattern> map : buf) {
                    tempHerePattern = map.get(tempDBPattern.apid);
                    t += tempHerePattern == null? 0: tempHerePattern.averageLevel;
                    i += tempHerePattern == null? 0: 1;
                }

                if (i > 0) {
                    t /= i;
                    distance += Math.pow(tempDBPattern.averageLevel - t, 2);
                    size++;
                }
            }

            if (size > 5) {
                resultSpotList.add(new SpotAndDist(Math.sqrt(distance)*dbPattern.length/size, spotIDList.get(n)));
            }
        }

        //リストをソート
        Collections.sort(resultSpotList, (lhs, rhs) -> -Double.compare(rhs.getDist(), lhs.getDist()));

    return resultSpotList;
    }

    @SuppressWarnings("UnusedDeclaration")
    private double correlation(List<Double> list1, List<Double> list2){
        double mean1 = 0,
                mean2 = 0,
                dis1 = 0,
                dis2 = 0;

        for(int i = 0; i < list1.size(); i++){
            mean1 += list1.get(i);
            mean2 += list2.get(i);
        }
        mean1 /= list1.size();
        mean2 /= list2.size();
        double correlation = 0;
        for(int i = 0; i < list1.size(); i++){
            correlation += (list1.get(i) - mean1)*(list2.get(i) - mean2);
            dis1 += Math.pow(list1.get(i) - mean1, 2);
            dis2 += Math.pow(list2.get(i) - mean2, 2);
        }
        correlation /= Math.sqrt(dis1)*Math.sqrt(dis2);
        return correlation;
    }
}
