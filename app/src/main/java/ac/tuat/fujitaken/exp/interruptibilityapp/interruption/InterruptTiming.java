package ac.tuat.fujitaken.exp.interruptibilityapp.interruption;

import android.content.Context;
import android.util.Log;

import java.net.UnknownHostException;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
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
import ac.tuat.fujitaken.exp.interruptibilityapp.flow.RegularThread;

;

/**
 * 通知タイミング検出用
 * Created by hi on 2015/11/17.
 */
public class InterruptTiming implements RegularThread.ThreadListener {

    //前の通知が出た時間
    long prevTime;
    //通知コントローラ．
    private NotificationController notificationController;

    //通知イベント検出用
    private Walking walking;
    private Screen screen;
    private Notify notify;
    private PC pc;

    //通知モードになっているか
    private boolean note;
    private boolean forceNote;  //s 追加：通知を強制する設定

    private UDPConnection udpConnection = null;
    private AllData mAllData;

    //コンストラクタ
    public InterruptTiming(Context context, AllData allData){
        this.mAllData = allData;
        //設定ファイルからモードを確認
        note = Settings.getAppSettings().isNoteMode();
        forceNote = Settings.getAppSettings().isForceNoteMode();  //s 追加：通知を強制する設定

        prevTime = System.currentTimeMillis()- Constants.NOTIFICATION_INTERVAL;
        notificationController = NotificationController.getInstance(allData, this);
        try {
            udpConnection = new UDPConnection(context);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        walking = new Walking();
        screen = new Screen(context);
        notify = new Notify(context);
        pc = new PC();
    }

    public void release(){
        notificationController.release();
    }

    public SaveData getEvaluationData() {
        return notificationController.getEvaluationData();
    }

    public AllData getAllData() {
        return mAllData;
    }

    /**
     * 1秒ごとに呼ばれる
     * データの更新，通知判定を行う
     */
    @Override
    //s クラス RegularThread からの implements
    public void run() {
        final RowData line = mAllData.newLine();
        Map<String, Data> map = mAllData.getData();

        int w = walking.judge(((BoolData)map.get(DataReceiver.WALK)).value);
        int s = screen.judge(mAllData.getData());
        int n = notify.judge(((StringData)map.get(DataReceiver.NOTIFICATION)).value);

        final int eventFlag = w | s | n;

        EvaluationData evaluationData = new EvaluationData();
        evaluationData.setValue(line);
        notificationController.save(evaluationData);

        if((eventFlag & (Walking.WALK_START | Walking.WALK_STOP | Screen.SCREEN_ON | Screen.SCREEN_OFF)) > 0) {
            eventTriggeredThread(eventFlag, evaluationData);
            mAllData.scan();
        }
    }

    private void eventTriggeredThread(final int eventFlag, final EvaluationData line){
        Thread thread = new Thread(()->{
                int event = eventFlag;
                boolean eval = false,
                        noteFlag = note    //通知モード
                                && !NotificationController.hasNotification,  //待機状態の通知なし
                        udpComm = (eventFlag & Screen.SCREEN_ON) > 0 || (eventFlag & Walking.WALK_START) > 0;   //UDP通信が必要かどうか

                String message = "null";
                if (udpConnection != null && udpComm) {

                    udpConnection.sendRequest(line);
                    message = udpConnection.receiveData();
                }
                event |= pc.judge(message);

                Log.d("EVENT", "Num is " + Integer.toBinaryString(event));
                Log.d("EVENT", Settings.getEventCounter().getEventName(event));

                if (noteFlag) {
                    //一時的に確率変更
                    double p = calcP(event);
                    Log.d("EVENT_P", "time " + (line.time - prevTime));
                    Log.d("EVENT_P", "P " + p);
                    Log.d("EVENT_P", "PC Flag " + pc.isPcFlag());
                    if (p >= 2  //評価数が平均より1/2以下では時間に関係なく通知
                            || forceNote  //s 追加：Setting_Exの 通知を強制 がオンになっていたら強制的に通知を配信する
                            || pcOpsFlag(event)
                            ||( Math.random() < p && line.time - prevTime > Constants.NOTIFICATION_INTERVAL)) {    //前の通知から一定時間経過
                        if (forceNote) {  //s 追加ここから
                            Log.e("forceNoteMode", "強制的に通知を配信 がオン");
                        }  //s 追加ここまで
                        notificationController.normalNotify(event, line);
                        eval = true;
                    }
                }
                if(!eval){
                    notificationController.saveEvent(event, line);
                }
            });
        thread.start();  //s 上までのthreadを開始する
    }

    private boolean pcOpsFlag(int event){
        if(pc.isPcFlag()) {
            if ((event & (Screen.SCREEN_ON)) > 0) {
                return Math.random() < 0.5;
            }
            else if ((event & (Screen.SCREEN_OFF)) > 0) {
                return true;
            }
        }
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
        EventCounter counter = Settings.getEventCounter();
        //eventが正常値以外なら確率0を返す
        if(counter.getEvaluations(event) == null) {
            return 0;
        }

        //対象となる遷移のサンプル数
        int s = counter.getEvaluations(event);
        s = s == 0? 1: s;

        /*隠しコマンド
        移動でサンプル数が1024なら無条件に確率0
         */
        if((event & (EventCounter.WALK_START_FLAG | EventCounter.WALK_STOP_FLAG)) >0){
            if(s == 1024){
                return 0;
            }
        }

        AppSettings settings = Settings.getAppSettings();

        //対象となるイベントに対応するイベント（例：PC→自発スマホなら、自発スマホ→PCが対応）
        int e = 0;
        boolean start = false;
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
                start = true;
                e = counter.getEvaluations(EventCounter.SP_TO_PC_BY_SELF_FLAG);
                break;
            case EventCounter.SP_TO_PC_BY_SELF_FLAG:
                e = counter.getEvaluations(EventCounter.PC_TO_SP_BY_SELF_FLAG);
                break;
            case EventCounter.PC_TO_SP_BY_NOTE_FLAG:
                start = true;
                e = counter.getEvaluations(EventCounter.SP_TO_PC_BY_NOTE_FLAG);
                break;
            case EventCounter.SP_TO_PC_BY_NOTE_FLAG:
                e = counter.getEvaluations(EventCounter.PC_TO_SP_BY_NOTE_FLAG);
                break;
            default:
                if(settings.isPcMode()){
                    return 0;
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
                        start = true;
                        e = counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_FLAG);
                        break;
                    case EventCounter.SELF_SCREEN_OFF_FLAG:
                        e = counter.getEvaluations(EventCounter.SELF_SCREEN_ON_FLAG);
                        break;
                    case EventCounter.NOTIFICATION_ON_FLAG:
                        start = true;
                        e = counter.getEvaluations(EventCounter.NOTIFICATION_OFF_FLAG);
                        break;
                    case EventCounter.NOTIFICATION_OFF_FLAG:
                        e = counter.getEvaluations(EventCounter.NOTIFICATION_ON_FLAG);
                        break;
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
