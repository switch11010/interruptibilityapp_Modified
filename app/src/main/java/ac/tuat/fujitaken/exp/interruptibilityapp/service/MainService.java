package ac.tuat.fujitaken.exp.interruptibilityapp.service;

import android.accessibilityservice.AccessibilityService;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;  //s 自作Log
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AccelerometerData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AllData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.WifiReceiver;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.flow.RegularThread;
import ac.tuat.fujitaken.exp.interruptibilityapp.flow.SaveTask;
import ac.tuat.fujitaken.exp.interruptibilityapp.interruption.InterruptTiming;


import static android.content.ContentValues.TAG;

/**
 * AccessibilityService
 * UI操作，ウィンドウの変化，通知を受け取る
 * 他のイベント，センサーはイベントレシーバに任せている
 * 終了処理を忘れずに
 */
@RequiresApi(api = Build.VERSION_CODES.DONUT)
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
        LogEx.d("Info", "MainService.onServiceConnected_1");  //s 追加
        super.onServiceConnected();
        LogEx.d("Info", "MainService.onServiceConnected_2e");  //s 追加
    }

    @Override
    public void onCreate() {
        LogEx.start();  //s 追加：ログの SaveData への記録を開始

        LogEx.d("Info", "MainService.onCreate_1");  //s 追加
        super.onCreate();
        LogEx.d("Info", "MainService.onCreate_2");  //s 追加

        //処理を続けるためにCPUのスリープをロックする
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MainService:MyWakeLock");  //s 文句を言われたのでnewWakeLockのタグを"MyWakeLock"から変更
        wakeLock.acquire();  //s ロック開始

        LogEx.d("Info", "MainService.onCreate_3");

        /**
         * 各インスタンスの初期化
         */
        loop_50 = new RegularThread();  //s ACC_LOOP_PERIOD(20ms) おき
        loop_1 = new RegularThread();  //s MAIN_LOOP_PERIOD(500ms) おき

        accelerometerData = new AccelerometerData(getApplicationContext());  //s getApplicationContext()：ApplicationのContext だか何かが返ってくるやつらしい
        allData = new AllData(getApplicationContext(), accelerometerData);
        interruptTiming = new InterruptTiming(getApplicationContext(), allData);

        saveTask = new SaveTask(getApplicationContext());

        //
        loop_50.setListener(allData.getWalkDetection());
        final SaveData save_50;  //s 再代入禁止なローカル変数の宣言
        if (Settings.getAppSettings().isAccSave()) {
            save_50 = new SaveData("Acc", accelerometerData.getHeader());
            loop_50.setListener(() -> save_50.addLine(accelerometerData.newLine()));
            saveTask.addData(save_50);
        }

        loop_1.setListener(allData);

        //割り込みタイミング制御用オブジェクト
        loop_1.setListener(interruptTiming);

        saveTask.addData(interruptTiming.getEvaluationData());  //s "Evaluation" カテゴリの SaveData を 定期的に記録するように登録する

        saveTask.addData(LogEx.getSaveData());  //s 追加：自作 LogEx クラスの SaveData を定期記録に登録

        //処理をスタート
        loop_50.start(Constants.ACC_LOOP_PERIOD, TimeUnit.MILLISECONDS);
        loop_1.start(Constants.MAIN_LOOP_PERIOD, TimeUnit.MILLISECONDS);
        saveTask.couldStart(Constants.SAVE_LOOP_PERIOD, TimeUnit.MINUTES);  //s 定期的に端末にデータを保存する処理

        Toast.makeText(getApplicationContext(), "記録開始", Toast.LENGTH_SHORT).show();
//        WifiReceiver.sendIP(getApplicationContext());
        //UDPConnection.startReceive();

        LogEx.d("Info", "MainService.onCreate_4e");  //s 追加
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    //s なんらかの Accessibility イベントが発生すると呼ばれる関数
    //s どの種類のイベントで呼ばれるかは設定ファイルに記載：res/xml/accessibility_service_config.xml
    //s 途中 AllData.put() を経由して AccessibilityData.put() が呼ばれる
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //AccessibilityServiceイベントをレシーバに受け渡し
        String focus = "None";
        AccessibilityNodeInfo root = this.getRootInActiveWindow();
        if (root != null) {
            if (root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) != null) {
                try {
                    focus = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT).getClassName().toString();
                } catch (Exception e) {
                    focus = "None";
                    LogEx.d("FindFocus", e.getLocalizedMessage());
                }
            } else {
                focus = "None";
            }
        }

        allData.put(event, focus);
    }


//    public boolean onTouchEvent(MotionEvent event) {
//        LogEx.d("test","タッチ中！");
//        //イベントの種類によって処理を振り分ける
//        switch (event.getAction()) {
//
//            //画面にタッチした時の処理
//            case MotionEvent.ACTION_DOWN: {
//                LogEx.d("test","タッチ中！");
//                break;
//            }
//            //画面のタッチが終わった時の処理
//            case MotionEvent.ACTION_UP: {
//                LogEx.d("test","notタッチ中！");
//                break;
//            }
//        }
//        return true;
//    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        LogEx.d("Info", "MainService.onDestroy_1");  //s 追加

        //UDPConnection.stop();
        //タイミング制御をストップ
        interruptTiming.release();

        //監視処理をストップ
        loop_1.stop();
        loop_50.stop();

        //必要なリスナーなどを開放
        accelerometerData.release();
        allData.release();

        //保存処理をストップ
        saveTask.stop();

        //s 追加：ログの SaveData への記録を終了
        LogEx.stop();

        Toast.makeText(getApplicationContext(), "記録終了", Toast.LENGTH_SHORT).show();

        //Wakelockを解放
        wakeLock.release();  //s CPUがスリープできるようになる

        LogEx.d("Info", "MainService.onDestroy_2");  //s 追加
        super.onDestroy();
        LogEx.d("Info", "MainService.onDestroy_3e");  //s 追加
    }
}
