package ac.tuat.fujitaken.exp.interruptibilityapp.data.status;

import android.content.Context;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.BoolData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.IntData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.DataReceiver;

/**
 * 画面オン・オフの検出  //s 追加：ついでにロック解除の有無も
 * Created by hi on 2015/11/25.
 */
public class Screen {

    public static final int SCREEN_ON = 1 << 3,
            SCREEN_OFF = 1 << 4;
    public static final int UNLOCK = 1 << 7;  //s 追加：ロック解除イベントを表すフラグのビット
    public static final int   LOCK = 1 << 8;  //s 追加：ロック設定イベントを表すフラグのビット（ロック解除状態からの遷移）

    private boolean prevState = true;  //s 前回の画面点灯状態
    private List<Integer> buffer = new ArrayList<>();  //s 画面を操作したかどうかの履歴を記憶するキュー？
    private int sumOps = 0;                            //s ↑の buffer 内に格納されている ops の合計数
    private boolean prevConnect = false;  //s 前回に充電中だったかの状態
    private String appName = "";
    private boolean prevUnlocked = true;  //s 追加：前回にロック解除済だったかの状態

    //s MainService.onCreate()→InterruptTiming() から呼ばれる
    public Screen(Context context){
        int screenOffTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Constants.SCREEN_OFF_TIME) / Constants.MAIN_LOOP_PERIOD - 1;
        sumOps = 1;
        for(int i = 0; i < screenOffTimeout -1; i++){
            buffer.add(0);
        }
        buffer.add(1);
        appName = context.getString(R.string.app_name);
    }

    //s 画面のオンオフ等の状態変更があったか判定をする
    //s InterruptTiming.run() から定期的に呼ばれる：引数で渡されるのは mAllData.getData
    //s 返り値は イベントのフラグの Screen に関連するビット
    public int judge(Map<String, Data> data){

        boolean latestValue = ((BoolData)data.get(DataReceiver.SCREEN_ON)).value;    //s 現在画面が点灯中かどうか
        boolean connect = ((BoolData)data.get(DataReceiver.POWER_CONNECTED)).value;  //s 現在充電中かどうか
        boolean nowUnlocked = ((BoolData)data.get(DataReceiver.UNLOCKED)).value;     //s 追加：現在ロック解除状態かどうか
        String noteApp = ((StringData)data.get(DataReceiver.NOTIFICATION)).value;

        int ops = (
        ((IntData)data.get(DataReceiver.VIEW_CLICKED)).value > 0
                | ((IntData)data.get(DataReceiver.VIEW_LONG_CLICKED)).value > 0
                | ((IntData)data.get(DataReceiver.VIEW_SCROLLED)).value > 0
                | ((IntData)data.get(DataReceiver.VIEW_TEXT_CHANGED)).value > 0
                | ((IntData)data.get(DataReceiver.VIEW_TEXT_SELECTION_CHANGED)).value > 0
        )? 1: 0;  //s 画面操作があったら 1 っぽい
        sumOps -= buffer.remove(0);
        sumOps += ops;
        buffer.add(ops);

        //s 画面がオンになったかどうか、オフになったかどうか（両方 false もあり得る）
        boolean on = latestValue && !prevState && !(prevConnect && !connect) && !appName.equals(noteApp),  //s 今回入＆前回切＆（前回接続＆今回非接続）ではない＆通知が自身の通知ではない
        off = !latestValue && prevState && sumOps > 0;  //s 今回切＆前回入＆無操作による画面切ではない
        boolean unlock = nowUnlocked && !prevUnlocked;  //s 追加：ロックが解除された
        boolean lock = !nowUnlocked && prevUnlocked;  //s 追加：ロックが設定された
        if(on){  //s 手動でオンにしたor通知でオンになった…？
            sumOps -= buffer.remove(0);  //s List（配列）の先頭の要素を削除＆その要素の数値を減算
            sumOps += 1;
            buffer.add(1);  //s List（配列）の最後尾に 1 を追加
        }

        prevConnect = connect;
        prevState = latestValue;
        prevUnlocked = nowUnlocked;  //s 追加
        int ret = 0;
        if(on){  //s 画面をつけた
            ret = SCREEN_ON;
        }
        else if(off){  //s 画面を消した
            ret = SCREEN_OFF;
        }
        ret = ret | (unlock ? UNLOCK : 0);  //s 追加：ロック解除がされたかどうかも返り値のビットに追加
        ret = ret | (lock ? LOCK : 0);  //s 追加：ロック設定についても同様
        return ret;
    }
}