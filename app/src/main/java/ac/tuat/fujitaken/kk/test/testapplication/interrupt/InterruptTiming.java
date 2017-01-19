package ac.tuat.fujitaken.kk.test.testapplication.interrupt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.UnknownHostException;

import ac.tuat.fujitaken.kk.test.testapplication.Constants;
import ac.tuat.fujitaken.kk.test.testapplication.ui.fragments.SettingFragment;
import ac.tuat.fujitaken.kk.test.testapplication.data.BoolData;
import ac.tuat.fujitaken.kk.test.testapplication.data.IntData;
import ac.tuat.fujitaken.kk.test.testapplication.data.RowData;
import ac.tuat.fujitaken.kk.test.testapplication.interrupt.decision.Phone;
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
    private Phone phone;
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
        phone = new Phone();
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
        allData.newLine();
        final RowData line = allData.getLatestLine();

        final int eventFlag = walking.judge(((BoolData)allData.getData().get(DataReceiver.WALK)).value)
                | screen.judge(allData.getData())
                | phone.judge(((IntData)allData.getData().get(DataReceiver.PHONE)).value);

        boolean eval = false;
        if(eventFlag > 0) {
            final long currentTime = System.currentTimeMillis();
            Log.d("EVENT_COUNTER", String.valueOf(eventFlag));
            if (udpConnection != null) {
                udpConnection.sendRequest();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String message = udpConnection.receiveData();
                        Log.d("UDP", "Received Time : " + (System.currentTimeMillis() - currentTime));
                        a(eventFlag, currentTime, line);
                    }
                }).start();
            }
            allData.scan();
        }

        if(!eval){
            notificationController.save(line);
        }
    }

    private boolean a(int eventFlag, long currentTime, RowData line){
        boolean eval = false;
        if ((eventFlag & EventCounter.PHONE_START_FLAG) > 0) {
                    notificationController.normalNotify(eventFlag, line);
            eval = true;
        } else if (note && !NotificationController.hasNotification && currentTime - prevTime > Constants.NOTIFICATION_INTERVAL){
            for(int i = 0; i < EventCounter.EVENT_FLAGS.length; i++){
                if((EventCounter.EVENT_FLAGS[i]&eventFlag) > 0){
                    double p = calcP(EventCounter.EVENTS[i]);
                    Log.d("EVENT",String.valueOf(eventFlag) + "\t" + String.valueOf(p));
                    if (Math.random() < p) {
                                notificationController.normalNotify(eventFlag,line);
                        eval = true;
                        break;
                    }
                }
            }
        }

        if(!eval){
            notificationController.saveEvent(eventFlag, line);
            eval = true;
        }
        return eval;
    }

    private double calcP(String event){
        /**
         * 電話以外は確率を求めてから通知
         * ただし，評価数が平均の2倍or1/2の場合は補正
         */
        int min = counter.getEvaluationMin();
        if (min == 0) {
            min = 1;
        }
        double denominator = min;
        int c = counter.getEvaluations(event);
        if (c == 0) {
            c = 1;
        }
        double p = denominator / c;

        double mean = counter.getEvaluationMean();
        double t = 1;
        switch (event) {
            case EventCounter.WALK_STOP:
                t = 1 - denominator / counter.getEvaluations(EventCounter.WALK_START);
                break;
            case EventCounter.EXT_SCREEN_OFF:
                t = 1 - denominator / counter.getEvaluations(EventCounter.EXT_SCREEN_OFF);
                break;
            case EventCounter.SELF_SCREEN_OFF:
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
        //とりあえず歩行時の確率を2倍に
        if(event == EventCounter.WALK_START || event == EventCounter.WALK_STOP){
            p *= 2;
        }
        return p;
    }
}
