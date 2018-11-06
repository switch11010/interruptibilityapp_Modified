package ac.tuat.fujitaken.exp.interruptibilityapp.data.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Notify;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.PC;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Screen;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Walking;

/**
 * イベントの検出回数・回答の回数を記録するクラス
 * Created by Kyouhei on 2015/12/03.
 */
public class EventCounter {

    //s 変数名→文字列 の組み合わせの定義
    //s 使われている場所は 下の EVENT_KEYS_FROM_FLAGS のキー としてのみ
    //s イベント発生回数の保存の際のキーにもなる
    private static final String
            WALK_START = "WALK_START",
            WALK_STOP  = "WALK_STOP",

            SELF_SCREEN_ON = "SELF_SCREEN_ON",
            NOTE_SCREEN_ON = "NOTE_SCREEN_ON",  //s NOTIFICATION_ON から変更
            SELF_SCREEN_OFF = "SELF_SCREEN_OFF",
            NOTE_SCREEN_OFF = "NOTE_SCREEN_OFF",  //s NOTIFICATION_OFF から変更

            WALK_TO_PC = "WALK_TO_PC",
            PC_TO_WALK = "PC_TO_WALK",

            SP_TO_PC_BY_SELF = "SP_TO_PC_BY_SELF",
            SP_TO_PC_BY_NOTE = "SP_TO_PC_BY_NOTE",
            PC_TO_SP_BY_SELF = "PC_TO_SP_BY_SELF",
            PC_TO_SP_BY_NOTE = "PC_TO_SP_BY_NOTE",

            //s 画面ロック解除＆再ロック 系
            SELF_UNLOCK = "SELF_UNLOCK",  //s 追加ここから
            NOTE_UNLOCK = "NOTE_UNLOCK",
            PC_TO_SP_BY_SELF_UNLOCK = "PC_TO_SP_BY_SELF_UNLOCK",
            PC_TO_SP_BY_NOTE_UNLOCK = "PC_TO_SP_BY_NOTE_UNLOCK",

            SELF_SCREEN_ON_UNLOCK = "SELF_SCREEN_ON_UNLOCK",
            NOTE_SCREEN_ON_UNLOCK = "NOTE_SCREEN_ON_UNLOCK",
            PC_TO_SP_BY_SELF_ON_UNLOCK = "PC_TO_SP_BY_SELF_ON_UNLOCK",
            PC_TO_SP_BY_NOTE_ON_UNLOCK = "PC_TO_SP_BY_NOTE_ON_UNLOCK",

            SELF_SCREEN_OFF_LOCK = "SELF_SCREEN_OFF_LOCK",
            NOTE_SCREEN_OFF_LOCK = "NOTE_SCREEN_OFF_LOCK",
            SP_TO_PC_BY_SELF_LOCK = "SP_TO_PC_BY_SELF_LOCK",
            SP_TO_PC_BY_NOTE_LOCK = "SP_TO_PC_BY_NOTE_LOCK";  //s 追加ここまで

    //s 状態遷移となるビットの組み合わせによる フラグの定義
    //s 使われている場所は InterruptTiming と 下の EVENT_KEYS_FROM_FLAGS のキー のみ
    public static final int
            //s スマホ単独
            WALK_START_FLAG = Walking.WALK_START,
            WALK_STOP_FLAG  = Walking.WALK_STOP,

            SELF_SCREEN_ON_FLAG = Screen.SCREEN_ON,
            NOTE_SCREEN_ON_FLAG = Screen.SCREEN_ON | Notify.NOTIFICATION,  //s NOTIFICATION_ON_FLAG から変更
            SELF_SCREEN_OFF_FLAG = Screen.SCREEN_OFF,
            NOTE_SCREEN_OFF_FLAG = Screen.SCREEN_OFF | Notify.NOTIFICATION,  //s NOTIFICATION_OFF_FLAG から変更

            //s 上の条件に PC の条件を追加
            PC_TO_WALK_FLAG = Walking.WALK_START | PC.FROM_PC,
            WALK_TO_PC_FLAG = Walking.WALK_STOP  | PC.FROM_PC,

            PC_TO_SP_BY_SELF_FLAG = Screen.SCREEN_ON | PC.FROM_PC,
            PC_TO_SP_BY_NOTE_FLAG = Screen.SCREEN_ON | Notify.NOTIFICATION | PC.FROM_PC,
            SP_TO_PC_BY_SELF_FLAG = Screen.SCREEN_OFF | PC.FROM_PC,
            SP_TO_PC_BY_NOTE_FLAG = Screen.SCREEN_OFF | Notify.NOTIFICATION | PC.FROM_PC,

            //s 追加ここから（上までの条件のうち、画面点灯・消灯 に関連するものに ロック解除・再ロック を追加）
            //s 画面のロック解除のみ（既に画面点灯されている状態）
            SELF_UNLOCK_FLAG = Screen.UNLOCK,
            NOTE_UNLOCK_FLAG = Screen.UNLOCK | Notify.NOTIFICATION,
            PC_TO_SP_BY_SELF_UNLOCK_FLAG = Screen.UNLOCK | PC.FROM_PC,
            PC_TO_SP_BY_NOTE_UNLOCK_FLAG = Screen.UNLOCK | Notify.NOTIFICATION | PC.FROM_PC,

            //s 画面点灯とロック解除が一気にされたパターン
            SELF_SCREEN_ON_UNLOCK_FLAG = SELF_SCREEN_ON_FLAG | Screen.UNLOCK,
            NOTE_SCREEN_ON_UNLOCK_FLAG = NOTE_SCREEN_ON_FLAG | Screen.UNLOCK,
            PC_TO_SP_BY_SELF_ON_UNLOCK_FLAG = PC_TO_SP_BY_SELF_FLAG | Screen.UNLOCK,
            PC_TO_SP_BY_NOTE_ON_UNLOCK_FLAG = PC_TO_SP_BY_NOTE_FLAG | Screen.UNLOCK,

            //s 再ロック＆画面消灯
            SELF_SCREEN_OFF_LOCK_FLAG = SELF_SCREEN_OFF_FLAG | Screen.LOCK,
            NOTE_SCREEN_OFF_LOCK_FLAG = NOTE_SCREEN_OFF_FLAG | Screen.LOCK,
            SP_TO_PC_BY_SELF_LOCK_FLAG = SP_TO_PC_BY_SELF_FLAG | Screen.LOCK,
            SP_TO_PC_BY_NOTE_LOCK_FLAG = SP_TO_PC_BY_NOTE_FLAG | Screen.LOCK;
            //s 追加ここまで

    //s 状態遷移となるビットの組み合わせによるフラグ → 文字列 の組み合わせの定義（連想配列）
    //s 読み出しのみ、直接使われているのは EventCounter 内でのみ
    private static final HashMap<Integer, String> EVENT_KEYS_FROM_FLAGS = new HashMap<Integer, String>(){
        {
            put(WALK_START_FLAG, WALK_START);
            put(WALK_STOP_FLAG,  WALK_STOP);

            put(SELF_SCREEN_ON_FLAG, SELF_SCREEN_ON);
            put(NOTE_SCREEN_ON_FLAG, NOTE_SCREEN_ON);  //s NOTIFICATION_ON_FLAG, NOTIFICATION_ON から変更
            put(SELF_SCREEN_OFF_FLAG, SELF_SCREEN_OFF);
            put(NOTE_SCREEN_OFF_FLAG, NOTE_SCREEN_OFF);  //s NOTIFICATION_OFF_FLAG, NOTIFICATION_OFF から変更

            put(WALK_TO_PC_FLAG, WALK_TO_PC);
            put(PC_TO_WALK_FLAG, PC_TO_WALK);

            put(SP_TO_PC_BY_SELF_FLAG, SP_TO_PC_BY_SELF);
            put(SP_TO_PC_BY_NOTE_FLAG, SP_TO_PC_BY_NOTE);
            put(PC_TO_SP_BY_SELF_FLAG, PC_TO_SP_BY_SELF);
            put(PC_TO_SP_BY_NOTE_FLAG, PC_TO_SP_BY_NOTE);

            //s 画面ロック解除＆再ロック 系
            put(SELF_UNLOCK_FLAG, SELF_UNLOCK);  //s 追加ここから
            put(NOTE_UNLOCK_FLAG, NOTE_UNLOCK);
            put(PC_TO_SP_BY_SELF_UNLOCK_FLAG, PC_TO_SP_BY_SELF_UNLOCK);
            put(PC_TO_SP_BY_NOTE_UNLOCK_FLAG, PC_TO_SP_BY_NOTE_UNLOCK);

            put(SELF_SCREEN_ON_UNLOCK_FLAG, SELF_SCREEN_ON_UNLOCK);
            put(NOTE_SCREEN_ON_UNLOCK_FLAG, NOTE_SCREEN_ON_UNLOCK);
            put(PC_TO_SP_BY_SELF_ON_UNLOCK_FLAG, PC_TO_SP_BY_SELF_ON_UNLOCK);
            put(PC_TO_SP_BY_NOTE_ON_UNLOCK_FLAG, PC_TO_SP_BY_NOTE_ON_UNLOCK);

            put(SELF_SCREEN_OFF_LOCK_FLAG, SELF_SCREEN_OFF_LOCK);
            put(NOTE_SCREEN_OFF_LOCK_FLAG, NOTE_SCREEN_OFF_LOCK);
            put(SP_TO_PC_BY_SELF_LOCK_FLAG, SP_TO_PC_BY_SELF_LOCK);
            put(SP_TO_PC_BY_NOTE_LOCK_FLAG, SP_TO_PC_BY_NOTE_LOCK);  //s 追加ここまで
        }
    };

    private Map<Integer, Integer> evaluations;  //s 状態遷移フラグ → 発生回数　の連想配列

    private SharedPreferences preferences;  //s イベント発生回数の記録・読み込み に使う

    private int min = 0;  //s イベントの中で 最も発生していないイベントの 発生回数
    private double mean = 0;  //s イベントの発生回数の 平均数

    /**
     * 記録済みのデータが有る場合は読み込み，無ければ初期化
     * @param context a
     */
    //s コンストラクタ
    //s Settings.onCreate() → Settings.settings() から呼ばれる
    @SuppressLint("UseSparseArrays")
    EventCounter(Context context){

        evaluations = new HashMap<>();

        preferences = PreferenceManager.getDefaultSharedPreferences(context);  //s なんか SharedPreferences のインスタンスを取得するらしい

        //s 記録されていた イベントの発生回数 を evaluations に読み込む
        for(Map.Entry<Integer, String> entry: EVENT_KEYS_FROM_FLAGS.entrySet()){
            evaluations.put(entry.getKey(), preferences.getInt(entry.getValue(), 0));

            //s D/EVENTS_FLAG: "WALK_TO_PC" is フラグのビット表記 ( 発生回数 )
            Log.d("EVENTS_FLAG", entry.getValue() + " is " + Integer.toBinaryString(entry.getKey()) + " ( " + evaluations.get(entry.getKey()) + " ) ");
        }
        calcEvaluation();  //s フィールド min, mean の更新
    }

    //s イベントのキーの文字列 から イベントの発生回数 を返す
    //s ItemFragment.onListItemClick() で呼ばれる
    public Integer getEvaluations(String e){

        for (Map.Entry<Integer, String> set: EVENT_KEYS_FROM_FLAGS.entrySet()){
            String value = set.getValue();
            if(value.equals(e)){
                return evaluations.get(set.getKey());
            }
        }
        return null;
    }

    //s イベントのフラグ から イベントの発生回数 を返す
    //s InterruptTiming.calcP() でめっちゃ呼ばれる
    //s イベントが evaluations に存在しない場合は null を返す
    public Integer getEvaluations(int e){
        return evaluations.get(e);
    }

    //s イベントのフラグ に対応するイベントの発生回数を 1 増加
    //s NotificationController で登録される BroadcastReceiver のレシーバ内で 通知に回答があったときに呼ばれる
    //s 通知に 30秒 以内に回答したとき のみ
    public void addEvaluations(int e){
        Integer t = evaluations.get(e);  //s イベント発生回数の 現在値
        if(t == null ){
            Log.e("EventCounter.addEval", "指定のフラグ " + Integer.toBinaryString(e) + " に対応する値が null");  //s 追加
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        evaluations.put(e, t+1);  //s フィールドの記録を 1 増加
        editor.putInt(EVENT_KEYS_FROM_FLAGS.get(e), t+1);  //s 設定ファイルの "WALK_START" 的なキーに保存
        editor.apply();
        calcEvaluation();  //s min と mean を更新
    }

    //s イベントの最小発生回数を返す（ただのゲッタ）
    public int getEvaluationMin() {
        return min;
    }

    //s 配列で指定されたイベントの中から 発生回数の最小のものを フィールド min に設定して、それを返す（未使用）
    public int getEvaluationMin(int[] events) {
        min = evaluations.get(events[0]);
        for(int e: events){
            int value = evaluations.get(e);
            Log.d("event", Integer.toBinaryString(e) + " is " + value);
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    //s イベントの平均発生回数を返す（ただのゲッタ）
    public double getEvaluationMean() {
        return mean;
    }

    //s フィールドの min と mean の値を更新する
    private void calcEvaluation(){
        min = evaluations.get(WALK_START_FLAG);
        mean = 0;
        int cnt = 0;  // evaluations[] のセットの総数
        for(Integer value: evaluations.values()){
            cnt++;
            mean += value;
            if (value < min) {
                min = value;
            }
        }
        mean /= cnt;
    }

    //s "WALK_START" 的な文字列 → 発生回数　の連想配列を返す
    //s ItemFragment.update() で使用
    public Map<String, Integer> getEvaluations() {
        //noinspection unchecked
        Map<String, Integer> ret = new TreeMap();
        for(Map.Entry<Integer, Integer> entry: evaluations.entrySet()){
            ret.put(EVENT_KEYS_FROM_FLAGS.get(entry.getKey()), entry.getValue());
        }
        return ret;
    }

    //s "WALK_START" 的な文字列 に対応するイベントの発生回数を c に変更 ＆ 端末に保存
    //s ItemFragment.onActivityResult() で使用
    public void putEvaluation(String e, int c){
        int event = 0;

        //s 文字列 e に対応するフラグを event に設定
        for (Map.Entry<Integer, String> entry: EVENT_KEYS_FROM_FLAGS.entrySet()){
            String value = entry.getValue();
            if(value.equals(e)){
                event = entry.getKey();
                break;
            }
        }
        if(evaluations.get(event) == null){
            Log.e("EventCounter.putEval", "指定のキー " + e + " が存在しない");  //s 追加
            return;
        }
        evaluations.put(event, c);  //s 更新
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(e, c);  //s 保存
        editor.apply();
        calcEvaluation();  //s min と mean の値を更新
    }

    //s イベントのフラグ から "WALK_START" 的な文字列 を返す
    //s ItemFragment.eventTriggeredThread() 内の Log で使用
    public String getEventName(int event){
        String ret = EVENT_KEYS_FROM_FLAGS.get(event);
        ret = (ret == null)? "": ret;
        return ret;
    }

    //s イベントの発生回数を 0 にリセットする（端末保存データも）
    //s ItemFragment.onOptionsItemSelected() で使用
    public void initialize(){
        SharedPreferences.Editor editor = preferences.edit();
        //editor.clear();  //s 追加：ってやると設定が全部消えるらしい（未確認）
        for(Map.Entry<Integer, String> set: EVENT_KEYS_FROM_FLAGS.entrySet()){
            evaluations.put(set.getKey(), 0);
            editor.putInt(set.getValue(), 0);
            editor.apply();  //s いらない気がする
        }
        editor.apply();
    }
}
