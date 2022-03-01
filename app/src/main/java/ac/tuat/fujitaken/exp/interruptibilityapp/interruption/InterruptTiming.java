package ac.tuat.fujitaken.exp.interruptibilityapp.interruption;

import android.content.Context;
import android.util.Log;

import java.net.UnknownHostException;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;  //s 自作Log
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.BoolData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AllData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.DataReceiver;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.EvaluationData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.AppSettings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.EventCounter;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Notify;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.PC;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Screen;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Walking;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.ActiveApp;  //s 追加：アクティブなアプリ
import ac.tuat.fujitaken.exp.interruptibilityapp.flow.RegularThread;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;


/**
 * 通知タイミング検出用
 * Created by hi on 2015/11/17.
 * Modified by s on 2018
 * Modified by ny on 2020
 */
public class InterruptTiming implements RegularThread.ThreadListener {

    //前の通知が出た時間
    long prevTime;  //s このクラスの他では NotificationController の ブロードキャストレシーバ から 通知回答時に格納される
    long prevTimeTmp; //前回通知のトリガイベントの発生時刻
    long prevTimeNoActive; //非遷移用
    long prevTimeOn; //前回ON
    //通知コントローラ．
    private NotificationController notificationController;

    //通知イベント検出用
    private Screen screen;
    private ActiveApp activeApp; //ny 追加：アプリ遷移イベント
    //通知モードになっているか
    private boolean note;
    private AllData mAllData;

    private boolean first5m; //ny 遷移から5分の最初のフラグ


    //コンストラクタ
    //s MainService.onCreate() から呼ばれる
    public InterruptTiming(Context context, AllData allData){
        this.mAllData = allData;
        //設定ファイルからモードを確認
        note = Settings.getAppSettings().isNoteMode();

        prevTime = System.currentTimeMillis();
        prevTimeTmp = System.currentTimeMillis();
        prevTimeNoActive = System.currentTimeMillis();
        prevTimeOn = 0;
        notificationController = NotificationController.getInstance(allData, this);

        screen = new Screen(context);
        activeApp = new ActiveApp(); //ny 追加
        first5m = true;
    }

    //s MainService.onDestroy() から呼ばれる
    public void release(){
        notificationController.release();
    }

    //s MainService.onCreate() の途中で呼ばれる
    public SaveData getEvaluationData() {
        return notificationController.getEvaluationData();
    }

    /**
     * MAIN_LOOP_PERIOD(500ms) おきに呼ばれる
     * データの更新，通知判定を行う
     */
    @Override  //s クラス RegularThread からの implements
    public void run() {
        final RowData line = mAllData.newLine();
        Map<String, Data> map = mAllData.getData();

        mAllData.setMemoryData();

        int s = screen.judge(mAllData.getData());     //s 画面の点灯消灯の有無
        int a = activeApp.judge(mAllData.getData());  //s 追加：アプリの切り替えの有無

        final int eventFlag = s | a;   //ny 追加：アプリ遷移
        EvaluationData evaluationData = new EvaluationData();  //s なんか RowData のやつ
        evaluationData.setValue(line);
        notificationController.save(evaluationData);  //s 通知配信無しでそのまま csv に書き込む？
        int eventFlagToNotify = Screen.SCREEN_ON | Screen.SCREEN_OFF | ActiveApp.APP_SWITCH;  //ny 追加
        if((eventFlag & eventFlagToNotify) > 0) {  //s 変更：ロック解除の追加（ロックはオフで兼用）
            eventTriggeredThread(eventFlag, evaluationData);
            first5m = true;
        }
        else{
            boolean isTimePassedNoActive = line.time - prevTimeNoActive > Constants.NOTIFICATION_INTERVAL_AS;  //ny 前の遷移から一定時間経過
            boolean isTimePassed = line.time - prevTime > Constants.NOTIFICATION_INTERVAL;  //s 前の通知から一定時間経過
            if(isTimePassedNoActive == true && isTimePassed == true && screen.prevState == true && first5m == true){
                final int NoActive = 1 << 10;
                NoActiveThread(NoActive, evaluationData);
                first5m = false;
            }
        }
    }

    private void NoActiveThread(final int eventFlag, final EvaluationData line)
    {
        AppSettings settings = Settings.getAppSettings();
        double rnd = Math.random();
        double p = (double)settings.getPort()/10;
        boolean pResult = rnd < p;
        LogEx.d("InterruptTiming.eTT", "rnd: " + rnd);
        LogEx.d("InterruptTiming.eTT", "p: " + p);
        LogEx.d("InterruptTiming.eTT", "通知配信判断: " + pResult );
        LogEx.d("InterruptTiming.eTT", "└非遷移条件");
        if(pResult == true) {
            notificationController.normalNotify(eventFlag, line);  //ny 通知を配信する
        }
        else{
            notificationController.saveEvent(eventFlag, line);  //ny 通知は配信しないが、記録には残しておく
        }
        prevTimeNoActive = System.currentTimeMillis();
    }

    //s 状態遷移のイベントが発生した際に呼ばれる Thread
    private void eventTriggeredThread(final int eventFlag, final EvaluationData line){
        Thread thread = new Thread(()->{
                int event = eventFlag;
                boolean eval = false,  //s 通知を配信したかどうかの一時記憶
                        noteFlag = note    //通知モード
                                && !NotificationController.hasNotification; //待機状態の通知なし

                prevTimeNoActive = System.currentTimeMillis();

                String str = "イベント発生：" + Integer.toBinaryString(event);
                str += " -> " + Settings.getEventCounter().getEventName(event);
                LogEx.d("InterruptTiming.eTT", str);
                LogEx.w("EvalData prevTime", new java.util.Date(prevTime).toString());
                LogEx.w("EvalData line.time", new java.util.Date(line.time).toString());  //s 追加ここまで

                if (noteFlag && Settings.getEventCounter().getEventName(event) != "" ) {
                    double p = calcP(event);
                    LogEx.d("EVENT_P", "time " + (line.time - prevTime));
                    LogEx.d("EVENT_P", "P " + p);

                    //s 変更・追加：だいたいこの辺から（変更前のよくわからない式は消滅）
                    double rnd = Math.random();  //s 乱数を一時記憶
                    boolean forceNote = Settings.getAppSettings().isForceNoteMode();  //s 通知を強制する設定
                    boolean pResult = rnd < p || p > 0 && forceNote;  //s 確率に当選、または、確率(>0)で false でも Setting_Ex の 通知を強制 がオンなら通知を配信
                    boolean isTimePassed = line.time - prevTime > Constants.NOTIFICATION_INTERVAL;  //s 前の通知から一定時間経過
                    boolean isTimePassedTmp = line.time - prevTimeTmp > Constants.NOTIFICATION_SLEEP;  //ny 前のトリガイベントから一定時間経過

                    LogEx.d("InterruptTiming.eTT", "rnd: " + rnd);
                    LogEx.d("InterruptTiming.eTT", "通知配信判断: " + (pResult && isTimePassed && isTimePassedTmp) );
                    LogEx.d("InterruptTiming.eTT", "├ rnd<p 確率当選？: " + pResult);
                    LogEx.d("InterruptTiming.eTT", "└ 前回通知から間を空けた？: " + isTimePassed);
                    LogEx.d("InterruptTiming.eTT", "└ 前回トリガイベントから間を空けた？: " + isTimePassedTmp);

                    if ( pResult && isTimePassed && isTimePassedTmp) {
                        if (forceNote) {
                            LogEx.e("forceNoteMode", "通知の配信を強制 がオン");
                        }  //s 変更・追加：だいたいこの辺まで
                        prevTimeTmp = System.currentTimeMillis();
                        notificationController.normalNotify(event, line);  //s 通知を配信する
                        eval = true;
                    }
                }
                if(!eval){
                    notificationController.saveEvent(event, line);  //s 通知は配信しないが、記録には残しておく
                }
            });
        thread.start();  //s 上までの処理を thread で開始する
    }

    public long getPrevTime() {
        return prevTime;
    }

    public void setPrevTime(long prevTime) {
        this.prevTime = prevTime;
    }

    /**
     * 通知確率を計算
     * 現在の確率は開始・終了あわせたときに2/3となるように設定
     * @param event 計算対象のイベント
     * @return  通知確率
     */
    private double calcP(int event){
        AppSettings settings = Settings.getAppSettings();  //s 変更：下から移動

        EventCounter counter = Settings.getEventCounter();

        //対象となる遷移のサンプル数
        int s = counter.getEvaluations(event);  //s 今回のイベントの発生回数
        s = s == 0? 1: s;

        int e = 0;  //s 対になるイベントの発生回数（旧型式のなごり）
        boolean start = false;  //s 歩行・スマホ への遷移 かどうか（旧型式のなごり）

        //s 追加ここから：通知配信の確率の計算 新型式（旧型式は消滅）
        int eventPattern = -999999;  //s 状態遷移イベントのパターンの種類（1, 2, 3）

        //s イベントのタイプごとの通知回答回数
        int eventCount1 = 1;
        int eventCount2 = 1;
        int eventCount3 = 1;  //s 値の正確性はそれほど重要ではないので 最少数として 1 を設定

        //ny 画面オン　の通知回答回数
        eventCount1 += counter.getEvaluations(EventCounter.SELF_SCREEN_ON_FLAG);
        eventCount1 += counter.getEvaluations(EventCounter.NOTE_SCREEN_ON_FLAG);

        //s 画面オン＆ロック状態 → 画面オン＆ロック解除状態　の通知回答回数
        eventCount1 += counter.getEvaluations(EventCounter.SELF_UNLOCK_FLAG);
        eventCount1 += counter.getEvaluations(EventCounter.NOTE_UNLOCK_FLAG);


        //s 画面オフ状態 → 画面オン＆ロック解除状態（複合型）　の通知回答回数
        eventCount1 += counter.getEvaluations(EventCounter.SELF_SCREEN_ON_UNLOCK_FLAG);  //s 複合型
        eventCount1 += counter.getEvaluations(EventCounter.NOTE_SCREEN_ON_UNLOCK_FLAG);  //s 複合型


        //s 画面オン＆ロック状態 → 画面オフ状態　の通知回答回数
        eventCount2 += counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_FLAG);
        eventCount2 += counter.getEvaluations(EventCounter.NOTE_SCREEN_OFF_FLAG);


        //s 画面オン＆ロック解除状態 → 画面オフ状態　の通知回答回数
        eventCount2 += counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_LOCK_FLAG);
        eventCount2 += counter.getEvaluations(EventCounter.NOTE_SCREEN_OFF_LOCK_FLAG);

        //ny 追加：アプリ遷移の通知回答回数
        eventCount3 += counter.getEvaluations(EventCounter.APP_SWITCH_FLAG);

        switch (event) {
            // 画面点灯 のみ
            case EventCounter.SELF_SCREEN_ON_FLAG:
            case EventCounter.NOTE_SCREEN_ON_FLAG:
                // ロック解除 のみ
            case EventCounter.SELF_UNLOCK_FLAG:
            case EventCounter.NOTE_UNLOCK_FLAG:
                // 画面点灯 と ロック解除 がいっぺんに行われた希少パターン
            case EventCounter.SELF_SCREEN_ON_UNLOCK_FLAG:  //s 複合型
            case EventCounter.NOTE_SCREEN_ON_UNLOCK_FLAG:  //s 複合型
                eventPattern = 1;
                break;

            // ロック未解除状態での 画面消灯
            case EventCounter.SELF_SCREEN_OFF_FLAG:
            case EventCounter.NOTE_SCREEN_OFF_FLAG:
                eventPattern = 2;
                break;

            // ロック解除済の状態での 画面消灯
            case EventCounter.SELF_SCREEN_OFF_LOCK_FLAG:
            case EventCounter.NOTE_SCREEN_OFF_LOCK_FLAG:
                eventPattern = 2;
                break;

            // アプリ切替
            case EventCounter.APP_SWITCH_FLAG:
                eventPattern = 3;
                break;

            default:
                return 0;
        }

        double p = -1;  //s 通知を配信する確率
        //ny 追加（11/18）一旦確率変更
        switch (eventPattern) {
            case 1:
                prevTimeOn = System.currentTimeMillis();
                p = (double)(eventCount2 + eventCount3) / (eventCount1 + eventCount2 + eventCount3);
                p = p * settings.getId()/10 * 1.3;
                break;
            case 2:
                p = (double)(eventCount1 + eventCount3) / (eventCount1 + eventCount2 + eventCount3);
                break;
            case 3:
                boolean isTimePassedOn = System.currentTimeMillis() - prevTimeOn > Constants.FROM_ON_TIME;  //s 前ONから一定時間経過
                if(isTimePassedOn  == true){
                    p = (double)(eventCount1 + eventCount2) / (eventCount1 + eventCount2 + eventCount3);
                    p = p * settings.getId()/10;
                }
                else
                {
                    p = 0;
                }
                break;
        }
        LogEx.d("calcP", "eventPattern: " + eventPattern);
        LogEx.d("calcP", "eventCount1: " + eventCount1);
        LogEx.d("calcP", "eventCount2: " + eventCount2);
        LogEx.d("calcP", "eventCount3: " + eventCount3);
        if (p >= 0) {
            return p * parseDouble(settings.getIpAddress());
        }
        return 1;
    }
}
