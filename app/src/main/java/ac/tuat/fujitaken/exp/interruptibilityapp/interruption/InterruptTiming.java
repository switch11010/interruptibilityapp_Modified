package ac.tuat.fujitaken.exp.interruptibilityapp.interruption;

import android.content.Context;

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

;

/**
 * 通知タイミング検出用
 * Created by hi on 2015/11/17.
 * Modified by s on 2018
 * Modified by ny on 2020
 */
public class InterruptTiming implements RegularThread.ThreadListener {

    //前の通知が出た時間
    long prevTime;  //s このクラスの他では NotificationController の ブロードキャストレシーバ から 通知回答時に格納される
    //通知コントローラ．
    private NotificationController notificationController;

    //通知イベント検出用
    private Walking walking;
    private Screen screen;
    private Notify notify;
    private PC pc;
    private ActiveApp activeApp; //ny 追加：アプリ遷移イベント
    //通知モードになっているか
    private boolean note;

    private UDPConnection udpConnection = null;
    private AllData mAllData;


    //コンストラクタ
    //s MainService.onCreate() から呼ばれる
    public InterruptTiming(Context context, AllData allData){
        this.mAllData = allData;
        //設定ファイルからモードを確認
        note = Settings.getAppSettings().isNoteMode();

        prevTime = System.currentTimeMillis()- Constants.NOTIFICATION_INTERVAL;
        notificationController = NotificationController.getInstance(allData, this);
        try {
            udpConnection = new UDPConnection(context);
        } catch (UnknownHostException e) {
            LogEx.e("InterruptTiming", "UDPConnectionに失敗");  //s 追加
            e.printStackTrace();
        }

        walking = new Walking();
        screen = new Screen(context);
        notify = new Notify(context);
        pc = new PC();
        activeApp = new ActiveApp(); //ny 追加
    }

    //s MainService.onDestroy() から呼ばれる
    public void release(){
        notificationController.release();
    }

    //s MainService.onCreate() の途中で呼ばれる
    public SaveData getEvaluationData() {
        return notificationController.getEvaluationData();
    }

    //s UDPConnection.notify() → NotificationController.normalNotify() で呼ばれるが使用されていない
    public AllData getAllData() {
        return mAllData;
    }

    /**
     * 1秒ごとに呼ばれる  //s 嘘：MAIN_LOOP_PERIOD(500ms) おきに呼ばれる
     * データの更新，通知判定を行う
     */
    @Override  //s クラス RegularThread からの implements
    public void run() {
        final RowData line = mAllData.newLine();
        Map<String, Data> map = mAllData.getData();

        int w = walking.judge(((BoolData)map.get(DataReceiver.WALK)).value);  //s 歩行の開始終了の有無
        int s = screen.judge(mAllData.getData());                             //s 画面の点灯消灯の有無
        int n = notify.judge(((StringData)map.get(DataReceiver.NOTIFICATION)).value);  //s 新しい通知の有無
        int a = activeApp.judge(mAllData.getData());  //s 追加：アプリの切り替えの有無

        final int eventFlag = w | s | n ;  //s 歩行・画面・通知 での状態変化のイベントのフラグ
        //final int eventFlag = w | s | n | a;   //ny 追加：アプリ遷移
        EvaluationData evaluationData = new EvaluationData();  //s なんか RowData のやつ
        evaluationData.setValue(line);
        notificationController.save(evaluationData);  //s 通知配信無しでそのまま csv に書き込む？

        //if((eventFlag & (Walking.WALK_START | Walking.WALK_STOP | Screen.SCREEN_ON | Screen.SCREEN_OFF)) > 0) {  //s コメントアウト
        int eventFlagToNotify = Walking.WALK_START | Walking.WALK_STOP | Screen.SCREEN_ON | Screen.SCREEN_OFF;  //s 分離
        eventFlagToNotify = eventFlagToNotify | Screen.UNLOCK;  //s 追加
        //eventFlagToNotify = eventFlagToNotify | Screen.UNLOCK | ActiveApp.APP_SWITCH;  //ny 追加
        if((eventFlag & eventFlagToNotify) > 0) {  //s 変更：ロック解除の追加（ロックはオフで兼用）
            eventTriggeredThread(eventFlag, evaluationData);
            mAllData.scan();  //s WifiReceiver.scan()：よくわからない
        } else if ( Settings.getAppSettings().isNoteOnAppChangeMode() && a > 0 ) {  //s 追加ここから：アプリの切り替えがあった場合、状態遷移として扱う
            eventTriggeredThread(a, evaluationData);  //s 他のイベントのフラグと混ぜると面倒くさいことになりそうなのでとりあえず単独でやる
            mAllData.scan();  //s 追加ここまで
        }
    }

    //s 状態遷移のイベントが発生した際に呼ばれる Thread
    private void eventTriggeredThread(final int eventFlag, EvaluationData line){
        Thread thread = new Thread(()->{
            int event = eventFlag;
            boolean eval = false,  //s 通知を配信したかどうかの一時記憶
                    noteFlag = note    //通知モード
                            && !NotificationController.hasNotification,  //待機状態の通知なし
                    udpComm = (eventFlag & Screen.SCREEN_ON) > 0 || (eventFlag & Walking.WALK_START) > 0;   //UDP通信が必要かどうか
            udpComm = udpComm || (eventFlag & Screen.UNLOCK) > 0;  //s 追加：ロック解除時にもUDP通信をしてみる
            udpComm = udpComm || (eventFlag & ActiveApp.APP_SWITCH) > 0 || (eventFlag & Screen.LOCK) > 0; //ny 追加：ロック時（画面OFF）とアプリ遷移時もUDP通信

            LogEx.d("InterruptTiming.eTT", "----------------------------------------");  //s 追加ここから（デバッグ）
            if (!noteFlag) {
                LogEx.d("InterruptTiming.eTT", "通知を配信しない判断");
                LogEx.d("InterruptTiming.eTT", "!noteMode: " + !note);
                LogEx.d("InterruptTiming.eTT", "NC.hasNotification: " + NotificationController.hasNotification);
            }  //s 追加ここまで

            //
//                if (event != ActiveApp.APP_SWITCH) {  //s if分岐追加：アプリ切替じゃないときだけUDPでPC操作の有無を調査するように変更
//                    String message = "null";
//                    if (udpConnection != null && udpComm) {
//
//                        udpConnection.sendRequest(line);
//                        message = udpConnection.receiveData();
//                    }
//                    event |= pc.judge(message);  //s PC のビットも追加
//                }  //s 追加：ifによる分岐の終了



            //LogEx.d("EVENT", "Num is " + Integer.toBinaryString(event));  //s コメントアウト
            //LogEx.d("EVENT", Settings.getEventCounter().getEventName(event));  //s コメントアウト
            String str = "イベント発生：" + Integer.toBinaryString(event);
            str += " -> " + Settings.getEventCounter().getEventName(event);
            LogEx.d("InterruptTiming.eTT", str);
                /*for (int i=0; i<line.data.size(); i++) {
                    LogEx.e("EvalData line.data", DataReceiver.NAMES[i] + ":\t" + line.data.get(i).getString());
                }*/
            LogEx.w("EvalData prevTime", new java.util.Date(prevTime).toString());
            LogEx.w("EvalData line.time", new java.util.Date(line.time).toString());  //s 追加ここまで

            //s 設定で通知モードが ON なら、通知を配信するか判断する　＆アプリ切替で通知配信がオンでアプリ切替イベントだった場合も追加
            //if (noteFlag) {  //s コメントアウト：変更前
            //if (noteFlag || event == ActiveApp.APP_SWITCH) {  //s 変更：アプリ切替のイベントの対応を追加
            if (noteFlag) {  //ny 一度戻す
                //一時的に確率変更  //s 恒久的な気がする
                //イベント時に常にPCと通信
                double p = 1;//calcP(event);
                LogEx.d("EVENT_P", "time " + (line.time - prevTime));
                LogEx.d("EVENT_P", "P " + p);
                LogEx.d("EVENT_P", "PC Flag " + pc.isPcFlag());

                //s 変更・追加：だいたいこの辺から（変更前のよくわからない式は消滅）
                double rnd = Math.random();  //s 乱数を一時記憶
                boolean forceNote = Settings.getAppSettings().isForceNoteMode();  //s 通知を強制する設定
                boolean pResult = rnd < p || p > 0 && forceNote;  //s 確率に当選、または、確率(>0)で false でも Setting_Ex の 通知を強制 がオンなら通知を配信
                boolean isTimePassed = line.time - prevTime > Constants.NOTIFICATION_INTERVAL;  //s 前の通知から一定時間経過
                LogEx.d("InterruptTiming.eTT", "rnd: " + rnd);
                LogEx.d("InterruptTiming.eTT", "通知配信判断: " + (pResult && isTimePassed) );
                LogEx.d("InterruptTiming.eTT", "├ rnd<p 確率当選？: " + pResult);
                LogEx.d("InterruptTiming.eTT", "└ 前回通知から間を空けた？: " + isTimePassed);

                if ( pcOpsFlag(event) || pResult && isTimePassed ) {  //s pcOpsFlag(event)：変更前のなごり（よくわかっていない）
                    if (forceNote) {
                        LogEx.e("forceNoteMode", "通知の配信を強制 がオン");
                    }  //s 変更・追加：だいたいこの辺まで

                    line.event = event;
                    //ny　変更：通知時にUDP通信
                    String message = "null";
                    if (udpConnection != null && udpComm) {
                        udpConnection.sendRequest(line);
//                            message = udpConnection.receiveData();
                    }

                    //notificationController.normalNotify(event, line);  //s 通知を配信する
                    eval = true;

                }
            }
            if(!eval){
                notificationController.saveEvent(event, line);  //s 通知は配信しないが、記録には残しておく
            }
        });
        thread.start();  //s 上までの処理を thread で開始する
    }

    //s 上の eventTriggeredThread() から呼ばれる
    private boolean pcOpsFlag(int event){
        if(pc.isPcFlag()) {
            if ((event & (Screen.SCREEN_ON)) > 0) {  //s PC使用＆スマホも使い始めた…？
                LogEx.d("InterruptTiming", "pcOpsFlag: random (on)");  //s 追加
                return Math.random() < 0.5;
            }
            else if ((event & (Screen.SCREEN_OFF)) > 0) {  //s PC使用＆スマホを使い終えた？
                LogEx.d("InterruptTiming", "pcOpsFlag: true (off)");  //s 追加
                return true;
            }
        }
        LogEx.d("InterruptTiming", "pcOpsFlag: false");  //s 追加
        return false;
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
        //eventが正常値以外なら確率0を返す
        if(counter.getEvaluations(event) == null) {  //s 返り値の Integer オブジェクトが null になった場合
            LogEx.e("InterruptTiming.calcP", "リストに無いイベント：" + Integer.toBinaryString(event));  //s 追加
            return 0;
        }

        //s 追加：まだ1回も画面を点灯させていなかったら 確率を 0 にする（サービス開始直後の消灯を飛ばすのが目的）
        if (screen.getScreenOnCount() == 0) {
            LogEx.w("InterruptTiming.calcP", "まだ画面を点灯させたことがない");
            return 0;
        }

        //対象となる遷移のサンプル数
        int s = counter.getEvaluations(event);  //s 今回のイベントの発生回数
        s = s == 0? 1: s;

        /*隠しコマンド
        移動でサンプル数が1024なら無条件に確率0  //s 追加：歩行時に通知を出さない設定がオンなら確率0
         */
        if((event & (EventCounter.WALK_START_FLAG | EventCounter.WALK_STOP_FLAG)) >0){
            if(s == 1024){
                LogEx.w("InterruptTiming.calcP", "歩行回数が1024回に到達");  //s 追加
                return 0;
            }
            if (settings.isNoNoteOnWalkMode()) {   //s 追加ここから
                LogEx.d("noNoteOnWalkMode", "歩行で通知を出さない がオン");
                return 0;
            }  //s 追加ここまで
        }

        //AppSettings settings = Settings.getAppSettings();  //s 上に移動

        //対象となるイベントに対応するイベント（例：PC→自発スマホなら、自発スマホ→PCが対応）
        int e = 0;  //s 対になるイベントの発生回数（旧型式のなごり）
        boolean start = false;  //s 歩行・スマホ への遷移 かどうか（旧型式のなごり）

        //s 追加ここから：通知配信の確率の計算 新型式（旧型式は消滅）
        int eventPattern = -999999;  //s 状態遷移イベントのパターンの種類（1, 2, 3）
        if ( (event & PC.FROM_PC) == 0 && settings.isPcMode()) {  //s pc.isPcFlag() && ～ と迷う
            LogEx.d("calcP", "PC関連イベントしか通知しない設定がオン ＆ PCが絡んでいる");
            return 0;  //s SettingFragment の左下のボタンが ON の場合は PCに関連したイベントでしか通知を出さない
        }

        //s イベントのタイプごとの通知回答回数
        int eventCount1 = 1;
        int eventCount2 = 1;
        int eventCount3 = 1;  //s 値の正確性はそれほど重要ではないので 最少数として 1 を設定
        int eventCount4 = 1; //ny 追加：アプリ遷移

        //s 画面オン＆ロック状態 → 画面オン＆ロック解除状態　の通知回答回数
        eventCount1 += counter.getEvaluations(EventCounter.SELF_UNLOCK_FLAG);
        eventCount1 += counter.getEvaluations(EventCounter.NOTE_UNLOCK_FLAG);
        //ny 今回は以下は使われない
//        eventCount1 += counter.getEvaluations(EventCounter.PC_TO_SP_BY_SELF_UNLOCK_FLAG);
//        eventCount1 += counter.getEvaluations(EventCounter.PC_TO_SP_BY_NOTE_UNLOCK_FLAG);

        //s 画面オフ状態 → 画面オン＆ロック解除状態（複合型）　の通知回答回数
        eventCount1 += counter.getEvaluations(EventCounter.SELF_SCREEN_ON_UNLOCK_FLAG);  //s 複合型
        eventCount1 += counter.getEvaluations(EventCounter.NOTE_SCREEN_ON_UNLOCK_FLAG);  //s 複合型
        //ny 今回は以下は使われない
//        eventCount1 += counter.getEvaluations(EventCounter.PC_TO_SP_BY_SELF_ON_UNLOCK_FLAG);  //s 複合型
//        eventCount1 += counter.getEvaluations(EventCounter.PC_TO_SP_BY_NOTE_ON_UNLOCK_FLAG);  //s 複合型

        //s 画面オン＆ロック状態 → 画面オフ状態　の通知回答回数
        eventCount2 += counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_FLAG);
        eventCount2 += counter.getEvaluations(EventCounter.NOTE_SCREEN_OFF_FLAG);
        //ny 今回は以下は使われない
//        eventCount2 += counter.getEvaluations(EventCounter.SP_TO_PC_BY_SELF_FLAG);
//        eventCount2 += counter.getEvaluations(EventCounter.SP_TO_PC_BY_NOTE_FLAG);

        //s 画面オン＆ロック解除状態 → 画面オフ状態　の通知回答回数
        eventCount3 += counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_LOCK_FLAG);
        eventCount3 += counter.getEvaluations(EventCounter.NOTE_SCREEN_OFF_LOCK_FLAG);
        //ny 今回は以下は使われない
//        eventCount3 += counter.getEvaluations(EventCounter.SP_TO_PC_BY_SELF_LOCK_FLAG);
//        eventCount3 += counter.getEvaluations(EventCounter.SP_TO_PC_BY_NOTE_LOCK_FLAG);

        //ny 追加：アプリ遷移の通知回答回数
        eventCount4 += counter.getEvaluations(EventCounter.APP_SWITCH_FLAG);

        switch (event) {
            // 画面点灯 のみ
            case EventCounter.SELF_SCREEN_ON_FLAG:
            case EventCounter.NOTE_SCREEN_ON_FLAG:
                //ny 今回は以下は使われない
//            case EventCounter.PC_TO_SP_BY_SELF_FLAG:
//            case EventCounter.PC_TO_SP_BY_NOTE_FLAG:
                eventPattern = 0;
                break;

            // ロック解除 のみ
            case EventCounter.SELF_UNLOCK_FLAG:
            case EventCounter.NOTE_UNLOCK_FLAG:
                //ny 今回は以下は使われない
//            case EventCounter.PC_TO_SP_BY_SELF_UNLOCK_FLAG:
//            case EventCounter.PC_TO_SP_BY_NOTE_UNLOCK_FLAG:
                eventPattern = 1;
                break;

            // ロック未解除状態での 画面消灯
            case EventCounter.SELF_SCREEN_OFF_FLAG:
            case EventCounter.NOTE_SCREEN_OFF_FLAG:
                //ny 今回は以下は使われない
//            case EventCounter.SP_TO_PC_BY_SELF_FLAG:
//            case EventCounter.SP_TO_PC_BY_NOTE_FLAG:
                eventPattern = 2;
                break;

            // ロック解除済の状態での 画面消灯
            case EventCounter.SELF_SCREEN_OFF_LOCK_FLAG:
            case EventCounter.NOTE_SCREEN_OFF_LOCK_FLAG:
                //ny 今回は以下は使われない
//            case EventCounter.SP_TO_PC_BY_SELF_LOCK_FLAG:
//            case EventCounter.SP_TO_PC_BY_NOTE_LOCK_FLAG:
                eventPattern = 3;
                break;

            // 画面点灯 と ロック解除 がいっぺんに行われた希少パターン
            case EventCounter.SELF_SCREEN_ON_UNLOCK_FLAG:  //s 複合型
            case EventCounter.NOTE_SCREEN_ON_UNLOCK_FLAG:  //s 複合型
                //ny 今回は以下は使われない
//            case EventCounter.PC_TO_SP_BY_SELF_ON_UNLOCK_FLAG:  //s 複合型
//            case EventCounter.PC_TO_SP_BY_NOTE_ON_UNLOCK_FLAG:  //s 複合型
                eventPattern = 1;
                break;

            // アプリ切替
            case EventCounter.APP_SWITCH_FLAG:
                eventPattern = 4;
                break;



            // 歩行開始（旧型式のなごり）
            //ny 今回は以下は使われない
//            case EventCounter.WALK_START_FLAG:
//                eventPattern = -1;
//                start = true;
//                e = counter.getEvaluations(EventCounter.WALK_STOP_FLAG);
//                break;
//            case EventCounter.PC_TO_WALK_FLAG:
//                eventPattern = -1;
//                start = true;
//                e = counter.getEvaluations(EventCounter.WALK_TO_PC_FLAG);
//                break;
//
//            // 歩行終了（旧型式のなごり）
//            case EventCounter.WALK_STOP_FLAG:
//                eventPattern = -2;
//                e = counter.getEvaluations(EventCounter.WALK_START_FLAG);
//                break;
//            case EventCounter.WALK_TO_PC_FLAG:
//                eventPattern = -2;
//                e = counter.getEvaluations(EventCounter.PC_TO_WALK_FLAG);
//                break;


            // よくわかんないの
            default:
                return 0;
        }

        double p = -1;  //s 通知を配信する確率
        //ny 追加（11/18）一旦確率変更
        switch (eventPattern) {
            case 0:
                p = 0.5;
                //p = 0;
                break;
            case 1:
                p = 0.5;
                //p = (double)eventCount3 / (eventCount1 + eventCount3);
                break;
            case 2:
                //int min13 = Math.min(eventCount1, eventCount3);
                //p = (double)min13 / (eventCount2 + min13);
                //p = (p + 2) / 3;
                p = 0.5;
                break;
            case 3:
                //p = (double)eventCount1 / (eventCount1 + eventCount3);
                //p = (p + 1) / 2;
                p = 0.5;
                break;
            case 4:
                p =  0.1;
                break;
        }
        LogEx.d("calcP", "eventPattern: " + eventPattern);
        LogEx.d("calcP", "eventCount1: " + eventCount1);
        LogEx.d("calcP", "eventCount2: " + eventCount2);
        LogEx.d("calcP", "eventCount3: " + eventCount3);
        LogEx.d("calcP", "eventCount4: " + eventCount4);
        if (p >= 0) {
            return p;  //s 確率が設定されたならそれを返して終了
        }
        //s 追加ここまで
        //s p に確率が設定されなかった 歩行 とかは 以下の旧計算式で計算する

        //対象の確率計算
        double p1 = (double)s/(s+e);
        //p1 = p1 * 2 / 3;

        //対象がスマホor移動開始ならそのままの確率
        if(start){
            return p1;
        }

        //対象がスマホor移動終了なら確率を補正
        /*double p2 = (double)e/(s+e);
        p2 = p2 * 2 / 3;
        p1 = p1 / (1 - p2);
        return p1;*/
        return 1;
    }
}
