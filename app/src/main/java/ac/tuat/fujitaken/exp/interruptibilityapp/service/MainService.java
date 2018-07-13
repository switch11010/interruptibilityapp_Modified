package ac.tuat.fujitaken.exp.interruptibilityapp.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AccelerometerData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AllData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.WifiReceiver;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.flow.RegularThread;
import ac.tuat.fujitaken.exp.interruptibilityapp.flow.SaveTask;
import ac.tuat.fujitaken.exp.interruptibilityapp.interruption.InterruptTiming;

/**
 * AccessibilityService
 * UI操作，ウィンドウの変化，通知を受け取る
 * 他のイベント，センサーはイベントレシーバに任せている
 * 終了処理を忘れずに
 */
public class MainService extends AccessibilityService {

    //CPUのスリープをロックする
    private PowerManager.WakeLock wakeLock;
    //一定時間でデータを更新する
    private RegularThread loop_1, loop_50;
    //データを保存する
    private SaveTask saveTask;
    //通知制御
    private InterruptTiming interruptTiming;
    //データ
    private AccelerometerData accelerometerData;
    private AllData allData;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //処理を続けるためにCPUのスリープをロックする
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();

        Log.d("Info", "onCreate");

        /**
         * 各インスタンスの初期化
         */
        loop_50 = new RegularThread();
        loop_1 = new RegularThread();

        accelerometerData = new AccelerometerData(getApplicationContext());
        allData = new AllData(getApplicationContext(), accelerometerData);
        interruptTiming = new InterruptTiming(getApplicationContext(), allData);

        saveTask = new SaveTask(getApplicationContext());

        //
        loop_50.setListener(allData.getWalkDetection());
        final SaveData save_50;
        if (Settings.getAppSettings().isAccSave()) {
            save_50 = new SaveData("Acc", accelerometerData.getHeader());
            loop_50.setListener(()-> save_50.addLine(accelerometerData.newLine()));
            saveTask.addData(save_50);
        }

        loop_1.setListener(allData);

        //割り込みタイミング制御用オブジェクト
        loop_1.setListener(interruptTiming);

        saveTask.addData(interruptTiming.getEvaluationData());

        //処理をスタート
        loop_50.start(Constants.ACC_LOOP_PERIOD, TimeUnit.MILLISECONDS);
        loop_1.start(Constants.MAIN_LOOP_PERIOD, TimeUnit.MILLISECONDS);
        saveTask.couldStart(Constants.SAVE_LOOP_PERIOD, TimeUnit.MINUTES);

        Toast.makeText(getApplicationContext(), "Service Start", Toast.LENGTH_SHORT).show();
        WifiReceiver.sendIP(getApplicationContext());
        //UDPConnection.startReceive();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //AccessibilityServiceイベントをレシーバに受け渡し
        allData.put(event);
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {

        //UDPConnection.stop();
        //タイミング制御をストップ
        interruptTiming.release();

        //関し処理をストップ
        loop_1.stop();
        loop_50.stop();

        //必要なリスナーなどを開放
        accelerometerData.release();
        allData.release();

        //保存処理をストップ
        saveTask.stop();

        Toast.makeText(getApplicationContext(), "Service Finish", Toast.LENGTH_SHORT).show();

        //Wakelockを解法
        wakeLock.release();
        super.onDestroy();
    }
}
