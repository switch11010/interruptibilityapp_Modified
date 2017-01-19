package ac.tuat.fujitaken.kk.test.testapplication.interrupt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * イベントの検出回数・回答の回数を記録するクラス
 * Created by Kyouhei on 2015/12/03.
 */
public class EventCounter {

    public static final String
            WALK_START = "WALK_START",
            WALK_STOP = "WALK_STOP",
            SELF_SCREEN_ON = "SELF_SCREEN_ON",
            NOTIFICATION = "NOTIFICATION",
            SELF_SCREEN_OFF = "SELF_SCREEN_OFF",
            EXT_SCREEN_OFF = "EXT_SCREEN_OFF",
            PHONE_START = "PHONE_START",
            PHONE_STOP = "PHONE_STOP",

            EVALUATION = "EVALUATION";

    public static final int
            WALK_START_FLAG = 1,
            WALK_STOP_FLAG = 1<<1,
            SELF_SCREEN_ON_FLAG = 1<<2,
            NOTIFICATION_FLAG = 1<<3,
            SELF_SCREEN_OFF_FLAG = 1<<4,
            EXT_SCREEN_OFF_FLAG = 1<<5,
            PHONE_START_FLAG = 1<<6,
            PHONE_STOP_FLAG = 1<<7;

    public static final String[] EVENTS = {
            WALK_START,
            WALK_STOP,
            SELF_SCREEN_ON,
            NOTIFICATION,
            SELF_SCREEN_OFF,
            EXT_SCREEN_OFF,
            PHONE_START,
            PHONE_STOP};

    public static final int[] EVENT_FLAGS = {
            WALK_START_FLAG,
            WALK_STOP_FLAG,
            SELF_SCREEN_ON_FLAG ,
            NOTIFICATION_FLAG,
            SELF_SCREEN_OFF_FLAG,
            EXT_SCREEN_OFF_FLAG,
            PHONE_START_FLAG,
            PHONE_STOP_FLAG};

    private Map<String, Integer> evaluations;

    /**
     * treemap内でイベントを並び替えるためのハッシュマップ．
     * (K, V) = (イベント名, 並び順)
     */
    private Map<String, Integer> names = new HashMap<>();

    private SharedPreferences preferences;

    private int min = 0;
    private double mean = 0;

    /**
     * 記録済みのデータが有る場合は読み込み，無ければ初期化
     * @param context
     */
    public EventCounter(Context context){

        evaluations = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return names.get(lhs).compareTo(names.get(rhs));
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        for(int i = 0; i < EVENTS.length; i++){
            String e = EVENTS[i];
            if(!PHONE_START.equals(e)) {
                names.put(e, i);
                evaluations.put(e, preferences.getInt(e + EVALUATION, 0));
            }
        }
        calcEvaluation();
    }

    public int getEvaluations(String e){
        return evaluations.get(e);
    }

    public void addEvaluations(int e){
        SharedPreferences.Editor editor = preferences.edit();
        for(int i = 0; i < EVENT_FLAGS.length; i++){
            if((EVENT_FLAGS[i]&e) > 0){
                int t = evaluations.get(EVENTS[i])+1;
                evaluations.put(EVENTS[i], t);
                editor.putInt(EVENTS[i] + EVALUATION, t).apply();
            }
        }
        calcEvaluation();
    }

    public int getEvaluationMin() {
        return min;
    }

    public double getEvaluationMean() {
        return mean;
    }

    private void calcEvaluation(){
        min = evaluations.get(WALK_START);
        mean = 0;
        for(Map.Entry<String, Integer> entry: evaluations.entrySet()){
            String e = entry.getKey();
            if(!(PHONE_START.equals(e) || PHONE_STOP.equals(e))) {
                mean += entry.getValue();
                if (entry.getValue() < min) {
                    min = entry.getValue();
                }
            }
        }
        mean /= 6;
    }

    public Map<String, Integer> getEvaluations() {
        return evaluations;
    }

    public void putEvaluation(String e, int c){
        evaluations.put(e, c);
        preferences.edit().putInt(e + EVALUATION, c).apply();
        calcEvaluation();
    }

    public void initialize(){
        SharedPreferences.Editor editor = preferences.edit();
        for(String e: EVENTS) {
            if(!PHONE_START.equals(e)) {
                evaluations.put(e, 0);
                editor.putInt(e + EVALUATION, 0).apply();
            }
        }
        editor.apply();
    }
}
