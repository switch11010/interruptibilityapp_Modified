package ac.tuat.fujitaken.exp.interruptibilityapp.interruption;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;  //s 自作Log
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AllData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.EvaluationData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Notify;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.PC;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.status.Screen;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.InterruptionNotification;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.QuestionActivity;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.fragments.QuestionFragment;

/**
 * 通知制御クラス
 * +α，監視データ+評価データを統合して記録するクラス
 * Created by hi on 2015/11/17.
 */
public class NotificationController {
    private static NotificationController instance = null;  //s インスタンスを保持する変数　定義の意図がナゾ（インスタンスが1つのみ生成の singleton っぽいけど少し違う）
    private InterruptionNotification interruptionNotification;
    private ScheduledExecutorService schedule;
    public SaveData evaluationSave;
    private LocalBroadcastManager localBroadcastManager;
    public static boolean hasNotification = false;
    private EvaluationData answerData,
            lateData,
            cancelData,
            answerLine;
    private InterruptTiming timing;

    private boolean cancelAskTask = false;  //s 追加：askTask の通知配信を中断するフラグ（やっつけ）


    /**
     * 通知への回答を受け取るレシーバ
     */
    //s QuestionActivity / QuestionFragment からアプリ内ブロードキャストが送信される
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //通知を開いたとき，通知の変更処理を中断する
            if(QuestionActivity.UPDATE_CANCEL.equals(action)){
                schedule.shutdownNow();
            }
            else if(QuestionFragment.BROADCAST_CANCEL_ACTION.equals(action)){
                cancelTask.run();
            }
            else{
                Bundle bundle = intent.getExtras();
                //通知へ30秒以内に回答したときの処理
                if(QuestionFragment.BROADCAST_ANSWER_ACTION.equals(action)){
                    answerData = (EvaluationData)bundle.getSerializable(EvaluationData.EVALUATION_DATA);
                    timing.prevTime = System.currentTimeMillis();
                    if (answerData != null) {
                        answerLine.setAnswer(answerData.clone());
                        Settings.getEventCounter().addEvaluations(answerData.event);
                        clearBuf();
                    }
                }
                //通知への回答期間を過ぎたときの処理
                else if(QuestionFragment.BROADCAST_ASK_ACTION.equals(action)){
                    lateData = (EvaluationData)bundle.getSerializable(EvaluationData.EVALUATION_DATA);
                    if (lateData != null) {
                        answerLine.setAnswer(lateData.clone());
                        clearBuf();
                    }
                }
            }
        }
    };

    //30秒経過したときの，通知の更新処理
    //s normalNotify() でスケジューラに渡される
    private Runnable askTask = new Runnable() {
        @Override
        public void run() {
            //s 追加：cancelAskTask に true が設定されていたら 通知の配信を中断する
            if (cancelAskTask) {
                LogEx.w("NC.askTask", "通知配信を中断");
                cancelAskTask = false;  //s 用は済んだので戻す
                return;
            }

            interruptionNotification.cancel();
            lateData.setValue(answerData);
//            Bundle bundle = new Bundle();
//            bundle.putSerializable(EvaluationData.EVALUATION_DATA, lateData);
//            interruptionNotification.cancelNotify(bundle);  //s 通知を配信する
//            schedule = Executors.newSingleThreadScheduledExecutor();
//            long delete = 60 * 1000;
//            schedule.schedule(cancelTask, delete, TimeUnit.MILLISECONDS);
        }
    };

    //通知への回答がなかったとき，通知を消す処理
    //s 上の BroadcastReceiver receiver でスケジューラに渡される
    private Runnable cancelTask = new Runnable() {
        @Override
        public void run() {
            interruptionNotification.cancel();
            answerLine.setAnswer(cancelData.clone());
            clearBuf();
        }
    };

    /**
     * 記録のバッファを開放する処理
     */
    private void clearBuf(){
        evaluationSave.lock = false;  //s 名前変更：rock → lock
        hasNotification = false;
    }

    //s インスタンスフィールドの SaveData のゲッタ（メソッド名がひどい）
    //s MainService.onCreate() から InterruptTiming.getEvaluationData() を経由して呼ばれる
    public SaveData getEvaluationData() {
        return evaluationSave;
    }

    //s インスタンスを生成 ＆ 生成したインスタンスを返す
    //s InterruptTiming のコンストラクタから呼ばれる
    public static NotificationController getInstance(AllData allData, InterruptTiming timing){
        //s if (instance == null) の条件を入れてもいいような気がする
        instance = new NotificationController(allData, timing);
        return instance;
    }

    //s インスタンスのゲッタ
    //s UDPConnection.notify() で呼ばれるけど使われない
    public static NotificationController getInstance(){
        return instance;
    }

    //s コンストラクタ（↑の getInstance(AllData, InterruptTiming) でインスタンスが生成される）
    private NotificationController(AllData allData, InterruptTiming timing){
        this.timing = timing;
        evaluationSave = new SaveData("Evaluation", "AnswerTime,Timing,News,Interrupt,Task,Location,UsePurpose,Comment,Event," + allData.getHeader());
        interruptionNotification = new InterruptionNotification(Settings.getContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(QuestionFragment.BROADCAST_ANSWER_ACTION);
        filter.addAction(QuestionFragment.BROADCAST_ASK_ACTION);
        filter.addAction(QuestionActivity.UPDATE_CANCEL);

        answerData = new EvaluationData();
        answerData.evaluation = 3;  //s 変更：デフォルトを 1 から変更（忙しいときにそのまま決定できるように）
        answerData.timing = 3; //ny 追加：タイミング
        answerData.news = 3; //ny　追加：ニュース
        answerData.task = "TV";
        answerData.location = "テーブル前";
        answerData.usePurpose = "ブラウジング";  //s 追加：スマホ使用目的

        lateData = new EvaluationData();
        lateData.evaluation = -3;
        cancelData = new EvaluationData();
        cancelData.evaluation = -2;

        localBroadcastManager = LocalBroadcastManager.getInstance(Settings.getContext());
        localBroadcastManager.registerReceiver(receiver, filter);
    }

    /**
     * レシーバ，バッファの開放と通知の削除
     */
    //s MainService.onDestroy() → InterruptTiming.release() から呼ばれる
    public void release() {
        if(hasNotification) {
            schedule.shutdownNow();
        }
        interruptionNotification.cancel();
        localBroadcastManager.unregisterReceiver(receiver);
        clearBuf();
        instance = null;
    }

    /**
     * 通知を出す
     * @param event 通知を出す要因のイベント
     */
    //s InterruptTiming.eventTriggeredThread() で、状態遷移が検知されて通知を配信したいときに呼ばれる
    public void normalNotify(int event, EvaluationData line){
        if(hasNotification){
            LogEx.e("NC.normalNotify", "通知が存在しているのに呼ばれた");  //s 追加
            return;
        }
        schedule = Executors.newSingleThreadScheduledExecutor();
        long delay = Constants.NOTIFICATION_THRESHOLD;
        //ny 通知無視されたら流す
        // schedule.schedule(askTask, delay, TimeUnit.MILLISECONDS);  //s delay (ms) 後に askTask 処理を開始する（通知無視の理由を聞く通知）
        Bundle bundle = new Bundle();

        line.event = event;
        answerLine = line;

        line.setAnswer(answerData);

        bundle.putSerializable(EvaluationData.EVALUATION_DATA, line);

        //s 割込み拒否度の評価を要求する通知を配信する（本命）
        //delay = ( (event & Screen.UNLOCK) > 0 ? 10000 : 0 );  //s 追加：通知配信を遅延させるミリ秒数
        //ny 情報提供開始から10秒後に評価アンケート配信
        delay = 10000;

        boolean isFailed = interruptionNotification.normalNotify(bundle, delay);  //s 変更：ロック解除時は10秒待機するように

        // 追加：通知の遅延配信に失敗（被り）したら askTask のスケジュールを停止する
        if (isFailed) {
            LogEx.w("NC.normalNotify", "通知の遅延配信に失敗したので askTask のスケジュールを停止");
            saveEvent(event, line);  //s そもそも通知を配信しようとしなかったことにする
            line.task = "";
            line.location = "";
            //schedule.shutdownNow();  //s なんかうまくいかない
            cancelAskTask = true;  //s やっつけ
        }

        hasNotification = true;
        evaluationSave.lock = true;  //s 名前変更：rock → lock
    }

    public InterruptTiming getTiming() {
        return timing;
    }

    //s UDPConnection.notify() で呼ばれるが使用されていない
    public void normalNotify(){
        if(hasNotification){
            return;
        }

        RowData lll = timing.getAllData().getLatestLine();
        EvaluationData line = new EvaluationData();
        line.setValue(lll);
        LogEx.d("Time", lll.time + "ms");

        schedule = Executors.newSingleThreadScheduledExecutor();
        long delay = Constants.NOTIFICATION_THRESHOLD;
        schedule.schedule(askTask, delay, TimeUnit.MILLISECONDS);
        Bundle bundle = new Bundle();

        line.event = PC.FROM_PC | Screen.SCREEN_ON | Notify.NOTIFICATION;
        answerLine = line;

        line.setAnswer(answerData);

        bundle.putSerializable(EvaluationData.EVALUATION_DATA, line);
        interruptionNotification.normalNotify(bundle);
        hasNotification = true;
        evaluationSave.lock = true;  //s 名前変更：rock → lock
    }

    //通知は出さないが，イベントを記録する
    public void saveEvent(int event, EvaluationData data){
        data.evaluation = -1;
        data.event = event;
        data.comment = "通知なし";
    }

    //イベントがなかったときは，データのみを記録する
    public void save(EvaluationData line){
        evaluationSave.addLine(line);
    }
}
