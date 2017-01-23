package ac.tuat.fujitaken.exp.interruptibilityapp.interrupt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.UnknownHostException;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.EvaluationData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.interrupt.decision.Notify;
import ac.tuat.fujitaken.exp.interruptibilityapp.interrupt.decision.PC;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.fragments.SettingFragment;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.BoolData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.interrupt.decision.Screen;
import ac.tuat.fujitaken.exp.interruptibilityapp.receiver.AllData;
import ac.tuat.fujitaken.exp.interruptibilityapp.receiver.DataReceiver;
import ac.tuat.fujitaken.exp.interruptibilityapp.interrupt.decision.Walking;
import ac.tuat.fujitaken.exp.interruptibilityapp.save.SaveData;
import ac.tuat.fujitaken.exp.interruptibilityapp.loop.RegularThread;

/**
 * 通知タイミング検出用
 * Created by hi on 2015/11/17.
 */
public class InterruptTiming implements RegularThread.ThreadListener {

    //前の通知が出た時間
    public long prevTime;
    //通知コントローラ．
    private NotificationController notificationController;

    //通知イベント検出用
    private Walking walking;
    private Screen screen;
    private Notify notify;
    private PC pc;

    //通知モードになっているか
    boolean note;

    private UDPConnection udpConnection = null;
    private EventCounter counter;
    private AllData allData;

    //コンストラクタ
    public InterruptTiming(Context context, AllData allData){
        this.allData = allData;
        //設定ファイルからモードを確認
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        note = preferences.getBoolean(SettingFragment.NOTE, true);

        prevTime = System.currentTimeMillis()- Constants.NOTIFICATION_INTERVAL;
        counter = new EventCounter(context);
        notificationController = new NotificationController(context, counter, allData, this);
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
        final RowData line = allData.newLine();

        int w = walking.judge(((BoolData)allData.getData().get(DataReceiver.WALK)).value);
        int s = screen.judge(allData.getData());
        int n = notify.judge(((StringData)allData.getData().get(DataReceiver.NOTIFICATION)).value);

        final int eventFlag = w | s | n;

        EvaluationData evaluationData = new EvaluationData();
        evaluationData.setValue(line);
        notificationController.save(evaluationData);

        if((eventFlag & 30) > 0) {
            Log.d("EVENT_COUNTER", String.valueOf(eventFlag));
            eventTriggeredThread(eventFlag, evaluationData);
            allData.scan();
        }
    }

    private void eventTriggeredThread(final int eventFlag, final EvaluationData line){
        new Thread(new Runnable() {
            @Override
            public void run() {
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

                if (noteFlag) {
                    double p = calcP(event);
                    Log.d("P", "P is " + p);
                    if (p >= 1  //評価数が他より1/2以下では時間に関係なく通知
                            ||( Math.random() < p && line.time - prevTime > Constants.NOTIFICATION_INTERVAL)) {    //前の通知から一定時間経過
                        notificationController.normalNotify(event, line);
                        eval = true;
                    }
                }
                if(!eval){
                    notificationController.saveEvent(event, line);
                }
            }
        }).start();
    }

    private double calcP(int event){
        /**
         * 電話以外は確率を求めてから通知
         * ただし，評価数が平均の2倍or1/2の場合は補正
         */
        if(counter.getEvaluations(event) == null){
            return 0;
        }

        int min = counter.getEvaluationMin();
        min = min == 0? 1: min;
        int c = counter.getEvaluations(event);
        c = c == 0? 1: c;
        double p = min / c;
        double mean = counter.getEvaluationMean();

        if (mean > 1) {
            if (c > mean * 1.5) {
                p = 0;
            } else if (c * 1.5 < mean) {
                p = 1;
            }
        }
        else {
            double t = 0;
            if((event & Walking.WALK_START) > 0){
                t = counter.getEvaluations(event ^ 6);
            }
            else if((event & Screen.SCREEN_ON) > 0){
                t = counter.getEvaluations(event ^ 24);
            }
            if (t != 0) {
                p /= 1 - min / t;
            }
        }
        return p;
    }
}
