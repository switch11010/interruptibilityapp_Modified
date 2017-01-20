package ac.tuat.fujitaken.kk.test.testapplication.interrupt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.UnknownHostException;

import ac.tuat.fujitaken.kk.test.testapplication.Constants;
import ac.tuat.fujitaken.kk.test.testapplication.data.StringData;
import ac.tuat.fujitaken.kk.test.testapplication.interrupt.decision.Notify;
import ac.tuat.fujitaken.kk.test.testapplication.ui.fragments.SettingFragment;
import ac.tuat.fujitaken.kk.test.testapplication.data.BoolData;
import ac.tuat.fujitaken.kk.test.testapplication.data.RowData;
import ac.tuat.fujitaken.kk.test.testapplication.interrupt.decision.Screen;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.AllData;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.DataReceiver;
import ac.tuat.fujitaken.kk.test.testapplication.interrupt.decision.Walking;
import ac.tuat.fujitaken.kk.test.testapplication.save.SaveData;
import ac.tuat.fujitaken.kk.test.testapplication.loop.Loop;

/**
 * 通知タイミング検出用
 * Created by hi on 2015/11/17.
 */
public class InterruptTiming implements Loop.LoopListener {

    //前の通知が出た時間
    public long prevTime;
    //通知コントローラ．
    private NotificationController notificationController;

    //通知イベント検出用
    private Walking walking;
    private Screen screen;
    private Notify notify;

    //通知モードになっているか
    boolean note;

    String message = "";

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
     * @param loop 監視しているインスタンス
     */
    @Override
    public void onLoop(Loop loop) {
        final RowData line = allData.newLine();

        int w = walking.judge(((BoolData)allData.getData().get(DataReceiver.WALK)).value);
        int s = screen.judge(allData.getData());
        int n = notify.judge(((StringData)allData.getData().get(DataReceiver.NOTIFICATION)).value);

        final int eventFlag = w | s | n;

        if((eventFlag & 1) > 0) {
            Log.d("EVENT_COUNTER", String.valueOf(eventFlag));
            eventTriggeredThread(eventFlag, line);
            allData.scan();
        }
        else{
            notificationController.save(line);
        }
    }

    private void eventTriggeredThread(final int eventFlag, final RowData line){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int event = eventFlag;
                boolean eval = false,
                        noteFlag = note    //通知モード
                                && !NotificationController.hasNotification  //待機状態の通知なし
                                && line.time - prevTime > Constants.NOTIFICATION_INTERVAL, //前の通知から一定時間経過
                        udpComm = (eventFlag & Screen.SCREEN_ON) > 0 || (eventFlag & Walking.WALK_START) > 0;   //UDP通信が必要かどうか

                if (noteFlag) {
                    if (udpConnection != null && udpComm) {

                        udpConnection.sendRequest();
                        message = udpConnection.receiveData();
                        Log.d("UDP", "Received Time : " + (System.currentTimeMillis() - line.time));
                    }
                    event |= evalPC(message);
                    double p = calcP(event);
                    if (Math.random() < p) {
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

    private int evalPC(String message){
        if(message.equals("")){
            return 0;
        }
        String[] params = message.split(":");

        long clickInterval = Long.parseLong(params[params.length - 1]),
                keyInterval = Long.parseLong(params[params.length - 2]);

        long pcInterval = clickInterval < keyInterval ? clickInterval : keyInterval;

        return (pcInterval < 60 * 1000)? 1 << 6: 0;
    }

    private double calcP(int event){
        /**
         * 電話以外は確率を求めてから通知
         * ただし，評価数が平均の2倍or1/2の場合は補正
         */
        /*
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
            switch (event) {
                case EventCounter.WALK_STOP_FLAG:
                    t = counter.getEvaluations(EventCounter.WALK_START_FLAG);
                    break;
                case EventCounter.NOTIFICATION_OFF_FLAG:
                    t = counter.getEvaluations(EventCounter.NOTIFICATION_OFF_FLAG);
                    break;
                case EventCounter.SELF_SCREEN_OFF_FLAG:
                    t = counter.getEvaluations(EventCounter.SELF_SCREEN_OFF_FLAG);
                    break;
                case EventCounter.WALK_TO_PC_FLAG:
                    t = counter.getEvaluations(EventCounter.PC_TO_WALK_FLAG);
                    break;
                case EventCounter.SP_TO_PC_BY_SELF_FLAG:
                    t = counter.getEvaluations(EventCounter.SP_TO_PC_BY_SELF_FLAG);
                    break;
                case EventCounter.SP_TO_PC_BY_NOTE_FLAG:
                    t = counter.getEvaluations(EventCounter.SP_TO_PC_BY_NOTE_FLAG);
                    break;
            }
            if (t != 0) {
                p /= 1 - min / t;
            }
        }
        return p;
        */
        return 1;
    }
}
