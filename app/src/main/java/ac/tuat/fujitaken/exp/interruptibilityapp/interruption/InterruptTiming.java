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

;

/**
 * 通知タイミング検出用
 * Created by hi on 2015/11/17.
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
        int a = ActiveApp.judge(mAllData.getData());  //s 追加：アプリの切り替えの有無

        final int eventFlag = w | s | n;  //s 歩行・画面・通知 での状態変化のイベントのフラグ

        EvaluationData evaluationData = new EvaluationData();  //s なんか RowData のやつ
        evaluationData.setValue(line);
        notificationController.save(evaluationData);  //s 通知配信無しでそのまま csv に書き込む？

        //if((eventFlag & (Walking.WALK_START | Walking.WALK_STOP | Screen.SCREEN_ON | Screen.SCREEN_OFF)) > 0) {  //s コメントアウト
        int eventFlagToNotify = Walking.WALK_START | Walking.WALK_STOP | Screen.SCREEN_ON | Screen.SCREEN_OFF;  //s 分離
        eventFlagToNotify = eventFlagToNotify | Screen.UNLOCK;  //s 追加
        if((eventFlag & eventFlagToNotify) > 0) {  //s 変更：ロック解除の追加（ロックはオフで兼用）
            eventTriggeredThread(eventFlag, evaluationData);
            mAllData.scan();  //s WifiReceiver.scan()：よくわからない
        } else if ( Settings.getAppSettings().isNoteOnAppChangeMode() && a > 0 ) {  //s 追加ここから：アプリの切り替えがあった場合、状態遷移として扱う
            eventTriggeredThread(a, evaluationData);  //s 他のイベントのフラグと混ぜると面倒くさいことになりそうなのでとりあえず単独でやる
            mAllData.scan();  //s 追加ここまで
        }
    }

    //s 状態遷移のイベントが発生した際に呼ばれる Thread
    private void eventTriggeredThread(final int eventFlag, final EvaluationData line){
        Thread thread = new Thread(()->{
                int event = eventFlag;
                boolean eval = false,  //s 通知を配信したかどうかの一時記憶
                        noteFlag = note    //通知モード
                                && !NotificationController.hasNotification,  //待機状態の通知なし
                        udpComm = (eventFlag & Screen.SCREEN_ON) > 0 || (eventFlag & Walking.WALK_START) > 0;   //UDP通信が必要かどうか
                if (!noteFlag) {  //s 追加ここから（デバッグ）
                    LogEx.d("InterruptTiming", "通知を配信しない判断");
                    LogEx.d("InterruptTiming", "!noteMode: " + !note);
                    LogEx.d("InterruptTiming", "NC.hasNotification: " + NotificationController.hasNotification);
                }  //s 追加ここまで

                if (event == ActiveApp.APP_SWITCH) {  //s 追加：アプリ切替じゃないときだけUDPでPC操作の有無を調査するように変更
                    String message = "null";
                    if (udpConnection != null && udpComm) {

                        udpConnection.sendRequest(line);
                        message = udpConnection.receiveData();
                    }
                    event |= pc.judge(message);  //s PC のビットも追加
                }  //s 追加：ifによる分岐の終了

                //LogEx.d("EVENT", "Num is " + Integer.toBinaryString(event));  //s コメントアウト
                //LogEx.d("EVENT", Settings.getEventCounter().getEventName(event));  //s コメントアウト
                LogEx.d("InterruptTiming.eTT", "--------------------");  //s 追加ここから
                String str = "イベント発生：" + Integer.toBinaryString(event);
                str += " -> " + Settings.getEventCounter().getEventName(event);
                LogEx.d("InterruptTiming.eTT", str);
                /*for (int i=0; i<line.data.size(); i++) {
                    LogEx.e("EvalData line.data", DataReceiver.NAMES[i] + ":\t" + line.data.get(i).getString());
                }*/
                LogEx.e("EvalData prevTime", new java.util.Date(prevTime).toString());
                LogEx.e("EvalData line.time", new java.util.Date(line.time).toString());  //s 追加ここまで

                //s 設定で通知モードが ON なら、通知を配信するか判断する　＆アプリ切替で通知配信がオンでアプリ切替イベントだった場合も追加
                //if (noteFlag) {  //s コメントアウト：変更前
                if (noteFlag || event == ActiveApp.APP_SWITCH) {  //s 変更：アプリ切替のイベントの対応を追加
                    //一時的に確率変更
                    double p = calcP(event);
                    LogEx.d("EVENT_P", "time " + (line.time - prevTime));
                    LogEx.d("EVENT_P", "P " + p);
                    LogEx.d("EVENT_P", "PC Flag " + pc.isPcFlag());

                    boolean forceNote = Settings.getAppSettings().isForceNoteMode();  //s 追加：通知を強制する設定
                    boolean isTimePassed = line.time - prevTime > Constants.NOTIFICATION_INTERVAL;  //s 追加：前の通知から一定時間経過（ifの判定式から独立）
                    /*if (p >= 2  //評価数が平均より1/2以下では時間に関係なく通知
                            || pcOpsFlag(event)
                            //||( Math.random() < p && line.time - prevTime > Constants.NOTIFICATION_INTERVAL)) {    //前の通知から一定時間経過  //s コメントアウト
                            || ( (Math.random() < p || p > 0 && forceNote) && isTimePassed )) {  //s 変更：確率(>0)で false でも Setting_Ex の 通知を強制 がオンなら通知を配信*/
                    double rnd = Math.random();  //s デバッグ用 ここから（上のブロックコメントが変更前）
                    boolean eval1 = rnd < p, eval2 = ( (eval1 || p > 0 && forceNote) && isTimePassed );
                    LogEx.d("InterruptTiming", "rnd: "+rnd);
                    LogEx.d("InterruptTiming", "eval1: "+eval1);
                    LogEx.d("InterruptTiming", "eval2: "+eval2);
                    LogEx.d("InterruptTiming", "├ rnd()<p 確率当選？: "+(eval1 || p > 0 && forceNote));
                    LogEx.d("InterruptTiming", "└ 前回通知から時間経過？: "+isTimePassed);
                    if (p >= 2  //評価数が平均より1/2以下では時間に関係なく通知
                            || pcOpsFlag(event)
                            //||( Math.random() < p && line.time - prevTime > Constants.NOTIFICATION_INTERVAL)) {    //前の通知から一定時間経過  //s コメントアウト
                            || eval2) {  //s 変更：確率(>0)で false でも Setting_Ex の 通知を強制 がオンなら通知を配信  //s デバッグ用 ここまで
                        if (forceNote) {  //s 追加ここから
                            LogEx.d("forceNoteMode", "通知の配信を強制 がオン");
                        }  //s 追加ここまで
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

    //s 上の eventTriggeredThread() から呼ばれる
    private boolean pcOpsFlag(int event){
        if(pc.isPcFlag()) {
            if ((event & (Screen.SCREEN_ON)) > 0) {  //s PC使用＆スマホも使い始めた…？
                LogEx.d("InterruptTiming", "pcOpsFlag: true1");  //s 追加
                return Math.random() < 0.5;
            }
            else if ((event & (Screen.SCREEN_OFF)) > 0) {  //s PC使用＆スマホを使い終えた？
                LogEx.d("InterruptTiming", "pcOpsFlag: true2");  //s 追加
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

        //対象となる遷移のサンプル数
        int s = counter.getEvaluations(event);  //s 今回のイベントの発生回数
        s = s == 0? 1: s;

        /*隠しコマンド
        移動でサンプル数が1024なら無条件に確率0  //s 追加：歩行時に通知を出さない設定がオンなら確率0
         */
        if((event & (EventCounter.WALK_START_FLAG | EventCounter.WALK_STOP_FLAG)) >0){
            if(s == 1024){
                return 0;
            }
            if (settings.isNoNoteOnWalkMode()) {   //s 追加ここから
                LogEx.d("noNoteOnWalkMode", "歩行で通知を出さない がオン");
                return 0;
            }  //s 追加ここまで
        }

        //AppSettings settings = Settings.getAppSettings();  //s 上に移動

        //対象となるイベントに対応するイベント（例：PC→自発スマホなら、自発スマホ→PCが対応）
        int e = 0;  //s 対になるイベントの発生回数
        boolean start = false;  //s 歩行・スマホ への遷移 かどうか
        switch(event) {
            //PCとの遷移
            case EventCounter.PC_TO_WALK_FLAG:
                start = true;
                e = counter.getEvaluations(EventCounter.WALK_TO_PC_FLAG);
                break;
            case EventCounter.WALK_TO_PC_FLAG:
                e = counter.getEvaluations(EventCounter.PC_TO_WALK_FLAG);
                break;
            case EventCounter.PC_TO_SP_BY_SELF_FLAG:
                /*start = true;
                e = counter.getEvaluations(EventCounter.SP_TO_PC_BY_SELF_FLAG);
                break;*/  //s コメントアウト
                return 0;  //s 変更：画面点灯だけでは通知は出さない
            case EventCounter.SP_TO_PC_BY_SELF_FLAG:
                e = counter.getEvaluations(EventCounter.PC_TO_SP_BY_SELF_FLAG);
                break;
            case EventCounter.PC_TO_SP_BY_NOTE_FLAG:
                /*start = true;
                e = counter.getEvaluations(EventCounter.SP_TO_PC_BY_NOTE_FLAG);
                break;*/  //s コメントアウト
                return 0;  //s 変更：画面点灯だけでは通知は出さない
            case EventCounter.SP_TO_PC_BY_NOTE_FLAG:
                e = counter.getEvaluations(EventCounter.PC_TO_SP_BY_NOTE_FLAG);
                break;

            //s 追加ここから
            case EventCounter.PC_TO_SP_BY_SELF_UNLOCK_FLAG:
                start = true;
                e = counter.getEvaluations(EventCounter.SP_TO_PC_BY_SELF_LOCK_FLAG);
                break;
            case EventCounter.SP_TO_PC_BY_SELF_LOCK_FLAG:
                e = counter.getEvaluations(EventCounter.PC_TO_SP_BY_SELF_UNLOCK_FLAG);
                break;
            case EventCounter.PC_TO_SP_BY_SELF_ON_UNLOCK_FLAG:  //s 複合型
                start = true;
                e = counter.getEvaluations(EventCounter.SP_TO_PC_BY_SELF_LOCK_FLAG);
                break;

            case EventCounter.PC_TO_SP_BY_NOTE_UNLOCK_FLAG:
                start = true;
                e = counter.getEvaluations(EventCounter.SP_TO_PC_BY_NOTE_LOCK_FLAG);
                break;
            case EventCounter.SP_TO_PC_BY_NOTE_LOCK_FLAG:
                e = counter.getEvaluations(EventCounter.PC_TO_SP_BY_NOTE_UNLOCK_FLAG);
                break;
            case EventCounter.PC_TO_SP_BY_NOTE_ON_UNLOCK_FLAG:  //s 複合型
                start = true;
                e = counter.getEvaluations(EventCounter.SP_TO_PC_BY_NOTE_LOCK_FLAG);
                break;
            //s 追加ここまで

            default:
                if(settings.isPcMode()){
                    return 0;  //s SettingFragment の左下のボタンが ON の場合は PCに関連したイベントでしか通知を出さない
                }
                switch (event){
                    //PCとは無関係な遷移
                    case EventCounter.WALK_START_FLAG:
                        start = true;
                        e = counter.getEvaluations(EventCounter.WALK_STOP_FLAG);
                        break;
                    case EventCounter.WALK_STOP_FLAG:
                        e = counter.getEvaluations(EventCounter.WALK_START_FLAG);
                        break;
                    case EventCounter.SELF_SCREEN_ON_FLAG:
                        /*start = true;
                        e = counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_FLAG);
                        break;*/  //s コメントアウト
                        return 0;  //s 変更：画面点灯だけでは通知は出さない
                    case EventCounter.SELF_SCREEN_OFF_FLAG:
                        e = counter.getEvaluations(EventCounter.SELF_SCREEN_ON_FLAG);
                        break;
                    case EventCounter.NOTE_SCREEN_ON_FLAG:
                        /*start = true;
                        e = counter.getEvaluations(EventCounter.NOTE_SCREEN_OFF_FLAG);
                        break;*/  //s コメントアウト
                        return 0;  //s 変更：画面点灯だけでは通知は出さない
                    case EventCounter.NOTE_SCREEN_OFF_FLAG:
                        e = counter.getEvaluations(EventCounter.NOTE_SCREEN_ON_FLAG);
                        break;

                    //s 追加ここから
                    case EventCounter.SELF_UNLOCK_FLAG:
                        start = true;
                        e = counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_LOCK_FLAG);
                        break;
                    case EventCounter.SELF_SCREEN_OFF_LOCK_FLAG:
                        e = counter.getEvaluations(EventCounter.SELF_UNLOCK_FLAG);
                        break;
                    case EventCounter.SELF_SCREEN_ON_UNLOCK_FLAG:  //s 複合型
                        start = true;
                        e = counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_LOCK_FLAG);
                        break;

                    case EventCounter.NOTE_UNLOCK_FLAG:
                        start = true;
                        e = counter.getEvaluations(EventCounter.NOTE_SCREEN_OFF_LOCK_FLAG);
                        break;
                    case EventCounter.NOTE_SCREEN_OFF_LOCK_FLAG:
                        e = counter.getEvaluations(EventCounter.NOTE_UNLOCK_FLAG);
                        break;
                    case EventCounter.NOTE_SCREEN_ON_UNLOCK_FLAG:  //s 複合型
                        start = true;
                        e = counter.getEvaluations(EventCounter.NOTE_SCREEN_OFF_LOCK_FLAG);
                        break;

                    case EventCounter.APP_SWITCH_FLAG:
                        return 1;
                    //s 追加ここまで

                    default:
                        return 0;
                }
        }

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
