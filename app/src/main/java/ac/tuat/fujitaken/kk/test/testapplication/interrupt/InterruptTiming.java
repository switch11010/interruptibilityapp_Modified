package ac.tuat.fujitaken.kk.test.testapplication.interrupt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.UnknownHostException;

import ac.tuat.fujitaken.kk.test.testapplication.Constants;
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

        final int eventFlag = walking.judge(((BoolData)allData.getData().get(DataReceiver.WALK)).value)
                | screen.judge(allData.getData());

        if(eventFlag > 0) {
            Log.d("EVENT_COUNTER", String.valueOf(eventFlag));
            if (udpConnection != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        udpConnection.sendRequest();
                        String message = udpConnection.receiveData();
                        Log.d("UDP", "Received Time : " + (System.currentTimeMillis() -line.time));
                        eventTrigger(eventFlag, line, message);
                    }
                }).start();
            }
            allData.scan();
        }
        else{
            notificationController.save(line);
        }
    }

    private void eventTrigger(int eventFlag, RowData line, String message){
        boolean eval = false,
                noteFlag = note    //通知モード
                && !NotificationController.hasNotification  //待機状態の通知なし
                && line.time - prevTime > Constants.NOTIFICATION_INTERVAL, //前の通知から一定時間経過
                transFromPC = evalPC(message);  //PCからの遷移かどうか

        if(noteFlag) {
            if (transFromPC) {
                double p = calcP(eventFlag);
                if (Math.random() < p) {
                    notificationController.normalNotify(eventFlag, line);
                    eval = true;
                }
            } else {

            }
        }

        if(!eval){
            notificationController.saveEvent(eventFlag, line);
        }
    }

    private boolean evalPC(String message){
        if(message.equals("")){
            return false;
        }
        String[] params = message.split(":");

        long clickInterval = Long.parseLong(params[params.length - 1]),
                keyInterval = Long.parseLong(params[params.length - 2]);

        long pcInterval = clickInterval < keyInterval ? clickInterval : keyInterval;

        return pcInterval < 60 * 1000;
    }

    private double calcP(int event){
        /**
         * 電話以外は確率を求めてから通知
         * ただし，評価数が平均の2倍or1/2の場合は補正
         */
        int min = counter.getEvaluationMin();
        if (min == 0) {
            min = 1;
        }
        double denominator = min;
        if(counter.getEvaluations(event) == null){
            return -1;
        }
        int c = counter.getEvaluations(event);
        if (c == 0) {
            c = 1;
        }
        double p = denominator / c;

        double mean = counter.getEvaluationMean();
        double t = 1;
        switch (event) {
            case EventCounter.WALK_STOP_FLAG:
                t = 1 - denominator / counter.getEvaluations(EventCounter.WALK_START);
                break;
            case EventCounter.NOTIFICATION_OFF_FLAG:
                t = 1 - denominator / counter.getEvaluations(EventCounter.NOTIFICATION_OFF);
                break;
            case EventCounter.SELF_SCREEN_OFF_FLAG:
                t = 1 - denominator / counter.getEvaluations(EventCounter.SELF_SCREEN_OFF);
                break;
        }
        if(t != 0){
            p /= t;
        }
        if (mean > 1) {
            if (c > mean * 1.5) {
                p = 0;
            } else if (c * 1.5 < mean) {
                p = 1;
            }
        }
        return p;
    }
}
