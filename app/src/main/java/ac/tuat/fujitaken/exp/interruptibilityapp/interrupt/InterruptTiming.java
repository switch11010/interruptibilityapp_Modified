package ac.tuat.fujitaken.exp.interruptibilityapp.interrupt;

import android.content.Context;
import android.util.Log;

import java.net.UnknownHostException;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.Flows.RegularThread;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.BoolData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AllData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.DataReceiver;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.EvaluationData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.EventCounter;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Notify;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.PC;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Screen;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Walking;

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

    private UDPConnection udpConnection = null;
    private AllData mAllData;

    //コンストラクタ
    public InterruptTiming(Context context, AllData allData){
        this.mAllData = allData;
        //設定ファイルからモードを確認
        note = Settings.getAppSettings().isNoteMode();

        prevTime = System.currentTimeMillis()- Constants.NOTIFICATION_INTERVAL;
        notificationController = new NotificationController(allData, this);
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

    /**
     * 1秒ごとに呼ばれる
     * データの更新，通知判定を行う
     */
    @Override
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
            Log.d("EVENT_COUNTER", String.valueOf(eventFlag));
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
                    Log.d("UDP", "Received Time : " + (System.currentTimeMillis() - line.time));
                }
                event |= pc.judge(message);

                Log.d("EVENT", "Num is " + Integer.toBinaryString(event));
                Log.d("EVENT", Settings.getEventCounter().getEventName(event));

                if (noteFlag) {
                    double p = calcP(event);
                    Log.d("P", "P is " + p);
                    if (p >= 2  //評価数が他より1/2以下では時間に関係なく通知
                            ||( Math.random() < p && line.time - prevTime > Constants.NOTIFICATION_INTERVAL)) {    //前の通知から一定時間経過
                        notificationController.normalNotify(event, line);
                        eval = true;
                    }
                }
                if(!eval){
                    notificationController.saveEvent(event, line);
                }
            });
        thread.start();
    }

    private double calcP(int event){
        /**
         * 電話以外は確率を求めてから通知
         * ただし，評価数が平均の2倍or1/2の場合は補正
         */
        EventCounter counter = Settings.getEventCounter();

        if(counter.getEvaluations(event) == null){
            return 0;
        }

        int min = counter.getEvaluationMin();
        min = min == 0? 1: min;
        int c = counter.getEvaluations(event);
        c = c == 0? 1: c;
        double p = min / c;
        double mean = counter.getEvaluationMean();

        if (mean > 1 && c >= mean + 10) {
            return 0;
        }

        double t = 0;
        if((event & Walking.WALK_START) > 0){
            t = counter.getEvaluations(event ^ (Walking.WALK_START | Walking.WALK_STOP));
        }
        else if((event & Screen.SCREEN_ON) > 0){
            t = counter.getEvaluations(event ^ (Screen.SCREEN_ON | Screen.SCREEN_OFF));
        }
        if (t != 0) {
            p /= 1 - min / t;
        }
        return p;
    }
}
