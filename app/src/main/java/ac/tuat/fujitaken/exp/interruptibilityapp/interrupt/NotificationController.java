package ac.tuat.fujitaken.exp.interruptibilityapp.interrupt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.EventCounter;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.EvaluationData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.InterruptionNotification;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.QuestionActivity;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.fragments.QuestionFragment;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AllData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;

/**
 * 通知制御くらす
 * だったが，最終的に，監視データ+評価データを統合して記録するクラス
 * Created by hi on 2015/11/17.
 */
public class NotificationController {
    private InterruptionNotification interruptionNotification;
    private ScheduledExecutorService schedule;
    public SaveData evaluationSave;
    private LocalBroadcastManager localBroadcastManager;
    public static boolean hasNotification = false;
    private EvaluationData answerData,
            lateData,
            cancelData,
            answerLine;
    public EventCounter counter;
    private InterruptTiming timing;
    private RowData saveLine;

    /**
     * 通知への回答を受け取るレシーバ
     */
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
                        counter.addEvaluations(answerData.event);
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
    private Runnable askTask = new Runnable() {
        @Override
        public void run() {
            interruptionNotification.cancel();
            lateData.setValue(answerData);
            Bundle bundle = new Bundle();
            bundle.putSerializable(EvaluationData.EVALUATION_DATA, lateData);
            interruptionNotification.cancelNotify(bundle);
            schedule = Executors.newSingleThreadScheduledExecutor();
            long delete = 60 * 1000;
            schedule.schedule(cancelTask, delete, TimeUnit.MILLISECONDS);
        }
    };

    //通知への回答がなかったとき，通知を消す処理
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
        evaluationSave.rock = false;
        hasNotification = false;
    }

    public SaveData getEvaluationData() {
        return evaluationSave;
    }

    public NotificationController(Context context, EventCounter counter, AllData allData, InterruptTiming timing){
        this.counter = counter;
        this.timing = timing;
        evaluationSave = new SaveData("Evaluation", "AnswerTime,Evaluation,Task,Location,Comment,Event," + allData.getHeader());
        interruptionNotification = new InterruptionNotification(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(QuestionFragment.BROADCAST_ANSWER_ACTION);
        filter.addAction(QuestionFragment.BROADCAST_ASK_ACTION);
        filter.addAction(QuestionActivity.UPDATE_CANCEL);

        answerData = new EvaluationData();
        answerData.evaluation = 1;
        answerData.task = "PC作業";
        answerData.location = "414";

        lateData = new EvaluationData();
        lateData.evaluation = -3;
        cancelData = new EvaluationData();
        cancelData.evaluation = -2;

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.registerReceiver(receiver, filter);
    }

    /**
     * レシーバ，バッファの開放と通知の削除
     */
    public void release() {
        if(hasNotification) {
            schedule.shutdownNow();
        }
        interruptionNotification.cancel();
        localBroadcastManager.unregisterReceiver(receiver);
        clearBuf();
    }

    /**
     * 通知を出す
     * @param event 通知を出す要因のイベント
     */
    public void normalNotify(int event, EvaluationData line){
        if(hasNotification){
            schedule.shutdownNow();
            cancelTask.run();
        }
        schedule = Executors.newSingleThreadScheduledExecutor();
        long delay = 30 * 1000;
        schedule.schedule(askTask, delay, TimeUnit.MILLISECONDS);
        Bundle bundle = new Bundle();

        line.event = event;
        answerLine = line;

        line.setAnswer(answerData);

        bundle.putSerializable(EvaluationData.EVALUATION_DATA, line);
        interruptionNotification.normalNotify(bundle);
        hasNotification = true;
        evaluationSave.rock = true;
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
