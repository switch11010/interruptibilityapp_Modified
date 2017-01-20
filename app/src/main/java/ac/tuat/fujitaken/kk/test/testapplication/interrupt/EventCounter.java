package ac.tuat.fujitaken.kk.test.testapplication.interrupt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public static final Map<Integer, String> EVENT_KEYS_FROM_FLAGS = new HashMap(){
        {
            put(WALK_START_FLAG, WALK_START);
            put(WALK_STOP_FLAG, WALK_STOP);
            put(SELF_SCREEN_ON_FLAG, SELF_SCREEN_ON);
            put(NOTIFICATION_FLAG, NOTIFICATION);
            put(SELF_SCREEN_OFF_FLAG, SELF_SCREEN_OFF);
            put(EXT_SCREEN_OFF_FLAG, EXT_SCREEN_OFF);
            put(PHONE_START_FLAG, PHONE_START);
            put(PHONE_STOP_FLAG, PHONE_STOP);
        }
    };

    private Map<Integer, Integer> evaluations;

    private SharedPreferences preferences;

    private int min = 0;
    private double mean = 0;

    /**
     * 記録済みのデータが有る場合は読み込み，無ければ初期化
     * @param context
     */
    public EventCounter(Context context){

        evaluations = new HashMap<>();

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        for(Map.Entry<Integer, String> set: EVENT_KEYS_FROM_FLAGS.entrySet()){
            evaluations.put(set.getKey(), preferences.getInt(set.getValue() + EVALUATION, 0));
        }
        calcEvaluation();
    }

    public Integer getEvaluations(String e){

        for (Map.Entry<Integer, String> set: EVENT_KEYS_FROM_FLAGS.entrySet()){
            if(set.getValue().equals(e)){
                return evaluations.get(set.getKey());
            }
        }
        return -1;
    }

    public Integer getEvaluations(int e){
        return evaluations.get(e);
    }

    public void addEvaluations(int e){
        Integer t = evaluations.get(EVENT_KEYS_FROM_FLAGS.get(e));
        if(t == null ){
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        evaluations.put(e, t+1);
        editor.putInt(EVENT_KEYS_FROM_FLAGS.get(e) + EVALUATION, t+1).apply();
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
        int cnt = 0;
        for(Integer value: evaluations.values()){
            cnt++;
            mean += value;
            if (value < min) {
                min = value;
            }
        }
        mean /= cnt;
    }

    public Map<Integer, Integer> getEvaluations() {
        return evaluations;
    }

    public void putEvaluation(String e, int c){
        int event = 0;
        for (Map.Entry<Integer, String> set: EVENT_KEYS_FROM_FLAGS.entrySet()){
            if(set.getValue().equals(e)){
                event = set.getKey();
                break;
            }
        }
        if(evaluations.get(event) == null){
            return;
        }
        evaluations.put(event, c);
        preferences.edit().putInt(e + EVALUATION, c).apply();
        calcEvaluation();
    }

    public void initialize(){
        SharedPreferences.Editor editor = preferences.edit();
        for(Map.Entry<Integer, String> set: EVENT_KEYS_FROM_FLAGS.entrySet()){
            evaluations.put(set.getKey(), 0);
            editor.putInt(set.getValue() + EVALUATION, 0).apply();
        }
        editor.apply();
    }
}
