package ac.tuat.fujitaken.kk.test.testapplication.interrupt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
            NOTIFICATION_ON = "NOTIFICATION_ON",
            SELF_SCREEN_OFF = "SELF_SCREEN_OFF",
            NOTIFICATION_OFF = "NOTIFICATION_OFF",

            WALK_TO_PC = "WALK_TO_PC",
            PC_TO_WALK = "PC_TO_WALK",

            SP_TO_PC_BY_SELF = "SP_TO_PC_BY_SELF",
            SP_TO_PC_BY_NOTE = "SP_TO_PC_BY_NOTE",
            PC_TO_SP_BY_SELF = "PC_TO_SP_BY_SELF",
            PC_TO_SP_BY_NOTE = "PC_TO_SP_BY_NOTE";

    public static final int
            WALK_START_FLAG = 1,
            WALK_STOP_FLAG = 1 << 1,

            SELF_SCREEN_ON_FLAG = 1 << 2,
            NOTIFICATION_ON_FLAG = 1 << 3,
            SELF_SCREEN_OFF_FLAG = 1 << 4,
            NOTIFICATION_OFF_FLAG = 1 << 5,

            WALK_TO_PC_FLAG = 1 << 6,
            PC_TO_WALK_FLAG = 1 << 7,

            SP_TO_PC_BY_SELF_FLAG = 1 << 8,
            SP_TO_PC_BY_NOTE_FLAG = 1 << 9,
            PC_TO_SP_BY_SELF_FLAG = 1 << 10,
            PC_TO_SP_BY_NOTE_FLAG = 1 << 11;

    public static final HashMap<Integer, String> EVENT_KEYS_FROM_FLAGS = new HashMap<Integer, String>(){
        {
            put(WALK_START_FLAG, WALK_START);
            put(WALK_STOP_FLAG, WALK_STOP);

            put(SELF_SCREEN_ON_FLAG, SELF_SCREEN_ON);
            put(NOTIFICATION_ON_FLAG, NOTIFICATION_ON);
            put(SELF_SCREEN_OFF_FLAG, SELF_SCREEN_OFF);
            put(NOTIFICATION_OFF_FLAG, NOTIFICATION_OFF);

            put(WALK_TO_PC_FLAG, WALK_TO_PC);
            put(PC_TO_WALK_FLAG, PC_TO_WALK);

            put(SP_TO_PC_BY_SELF_FLAG, SP_TO_PC_BY_SELF);
            put(SP_TO_PC_BY_NOTE_FLAG, SP_TO_PC_BY_NOTE);
            put(PC_TO_SP_BY_SELF_FLAG, PC_TO_SP_BY_SELF);
            put(PC_TO_SP_BY_NOTE_FLAG, PC_TO_SP_BY_NOTE);
        }
    };

    private Map<Integer, Integer> evaluations;

    private SharedPreferences preferences;

    private int min = 0;
    private double mean = 0;

    /**
     * 記録済みのデータが有る場合は読み込み，無ければ初期化
     * @param context a
     */
    public EventCounter(Context context){

        evaluations = new HashMap<>();

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        for(Map.Entry<Integer, String> entry: EVENT_KEYS_FROM_FLAGS.entrySet()){
            evaluations.put(entry.getKey(), preferences.getInt(entry.getValue(), 0));
        }
        calcEvaluation();
    }

    public Integer getEvaluations(String e){

        for (Map.Entry<Integer, String> set: EVENT_KEYS_FROM_FLAGS.entrySet()){
            if(set.getValue().equals(e)){
                return evaluations.get(set.getKey());
            }
        }
        return null;
    }

    Integer getEvaluations(int e){
        return evaluations.get(e);
    }

    void addEvaluations(int e){
        Integer t = evaluations.get(e);
        if(t == null ){
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        evaluations.put(e, t+1);
        editor.putInt(EVENT_KEYS_FROM_FLAGS.get(e), t+1).apply();
        calcEvaluation();
    }

    int getEvaluationMin() {
        return min;
    }

    double getEvaluationMean() {
        return mean;
    }

    private void calcEvaluation(){
        min = evaluations.get(WALK_START_FLAG);
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

    public Map<String, Integer> getEvaluations() {
        Map<String, Integer> ret = new TreeMap();
        for(Map.Entry<Integer, Integer> entry: evaluations.entrySet()){
            ret.put(EVENT_KEYS_FROM_FLAGS.get(entry.getKey()), entry.getValue());
        }
        return ret;
    }

    public void putEvaluation(String e, int c){
        int event = 0;

        for (Map.Entry<Integer, String> entry: EVENT_KEYS_FROM_FLAGS.entrySet()){
            if(entry.getValue().equals(e)){
                event = entry.getKey();
                break;
            }
        }
        if(evaluations.get(event) == null){
            return;
        }
        evaluations.put(event, c);
        preferences.edit().putInt(e, c).apply();
        calcEvaluation();
    }

    public void initialize(){
        SharedPreferences.Editor editor = preferences.edit();
        for(Map.Entry<Integer, String> set: EVENT_KEYS_FROM_FLAGS.entrySet()){
            evaluations.put(set.getKey(), 0);
            editor.putInt(set.getValue(), 0).apply();
        }
        editor.apply();
    }
}
