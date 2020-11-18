package ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;  //s 自作Log
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.interruption.NotificationController;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment;

/**
 * Created by hi on 2015/10/28.
 *
 * 通知作成・発生クラス
 * 適切なタイミングで呼び出せば通知を作成
 * 通知をタップすれば質問用ダイアログ表示のためのアクティビティが起動する
 */
public class InterruptionNotification {
    private Context context;
    private int volume,
    currentRinger,
    currentVolume;

    public static final String TYPE = "NOTIFICATION_TYPE";
    private static final String NOTIFICATION_TAG = "INTERRUPTION_NOTIFICATION";  //s 変更：用途がナゾだけど外部で使われないので private に変更

    private static final int REQUEST_CODE = 753,
                                NOTIFICATION_ID = 357;

    private NotificationManager notificationManager;
    private AudioManager audioManager;

    public InterruptionNotification(Context context){
        this.context = context;
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        volume = Settings.getAppSettings().getVolume();

        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    //s 配信済みの通知をひっこめる
    public void cancel(){
        notificationManager.cancel(NOTIFICATION_ID);
    }

    //s 普通の割込み拒否度の評価 の通知を配信する
    //s NotificationController.normalNotify() で呼ばれるが使用されていない
    public boolean normalNotify(final Bundle bundle){  //s 変更：返り値を追加（遅延時間指定が無いので常にfalse）
        return notification(bundle, true, 0);  //s 変更：通知配信の遅延時間の引数を追加、返り値を追加
    }
    //s 追加：通知配信の遅延時間を指定できるようにしたver
    //s NotificationController.normalNotify(int event, EvaluationData) で呼ばれる
    //s 返り値：通知を遅延させた結果、他の通知と被って配信できなかったら true
    public boolean normalNotify(final Bundle bundle, long delayMillis){
        return notification(bundle, true, delayMillis);
    }

    //s なんで通知無視したんや の通知を配信する
    //s NotificationController.normalNotify(int event, EvaluationData) 内でスケジューラに設定される askTask から呼ばれる
    public boolean cancelNotify(final Bundle bundle){  //s 変更：返り値を追加（遅延時間指定が無いので常にfalse）
        return notification(bundle, false, 0);  //s 変更：通知配信の遅延時間の引数を追加
    }
    //s 追加：通知配信の遅延時間を指定できるようにしたver（未使用）
    //s 返り値：通知を遅延させた結果、他の通知と被って配信できなかったら true
    public boolean cancelNotify(final Bundle bundle, long delayMillis){
        return notification(bundle, true, delayMillis);
    }

    //s いろいろパラメータを設定して、通知を配信する
    //s 上の normalNotify(), cancelNotify() から呼ばれる
    private boolean notification(Bundle bundle, boolean mode, long delayMillis) {  //s 変更：通知配信の遅延時間の引数と返り値（true：通知配信被りで失敗）を追加
        Intent intent = new Intent(context, QuestionActivity.class);
        bundle.putBoolean(TYPE, mode);
        intent.putExtras(bundle);
        PendingIntent pi = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("評価アンケート")
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(Integer.MAX_VALUE)  //s 優先度を激MAXにすることで常に最上位に表示されるようになるハイパーテクニックらしい
                .setVibrate(makeVibrationPattern(mode))  //s 変更：バイブのパターンの定義の makeVibrationPattern への分離 ＆ パターンの追加
                .setLights(Color.rgb(255, 215, 0), 3000, 3000)     //金色っぽい色
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setOnlyAlertOnce(true);

        if(mode){
            builder.setContentText("今の情報提供はどうでしたか？")
                    .setTicker("今の情報提供はどうでしたか？");
        }
        else{
            builder.setContentText("回答時間を過ぎました")
                    .setTicker("回答時間を過ぎました");
        }

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        //noinspection deprecation
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "InterruptionNotification:" + NOTIFICATION_TAG);  //s 変更：文句を言われたのでtagを変更
        wakelock.acquire();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        currentRinger = audioManager.getRingerMode();
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        if(volume == 0){
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
        else{
            try {  //s 追加：サイレントモード中の音量変更での例外が投げられたら無視する（雑）
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);  //s ここは変更なし
            } catch (SecurityException e) {  //s 追加ここから
                if (!e.getMessage().contains("Not allowed to change Do Not Disturb state")) {
                    LogEx.e("INotification.noti()", "setRingerMode で予想外の例外：" + e.getMessage());
                    throw e;  //s 思ってたのと違うやつはそのまま例外を中継
                }
                LogEx.e("INotification.noti()", "サイレントモード中の音量変更での例外を無視");  //s 例外を無視した記録
            }  //s 追加ここまで
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
        }

        //s 通知配信の遅延が指定されているなら待つ
        if (delayMillis > 0) {  //s 追加ここから
            try {
                LogEx.d("IntrrptNote.notifi()", "sleep開始");
                Thread.sleep(delayMillis);
                LogEx.d("IntrrptNote.notifi()", "sleep終了");
            } catch (InterruptedException e) {
                LogEx.e("IntrrptNote.notifi()", "InterruptedException");
                e.printStackTrace();
            }

            //s 他のイベントだかで通知が既に出ていたら配信しないで終了（もうちょっとマシな方法がある気がする）
            if (NotificationController.hasNotification) {
                LogEx.d("IntrrptNote.notifi()", "sleepしてる間に他の通知が出た");
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume, 0);
                audioManager.setRingerMode(currentRinger);
                wakelock.release();
                return true;  //s 中断
            }

            //s 遅延している間にサービスが停止されていたら配信しないで終了
            if (!SettingFragment.isServiceActive(Settings.getContext())) {
                LogEx.d("IntrrptNote.notifi()", "通知配信遅延中にサービス終了");
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume, 0);
                audioManager.setRingerMode(currentRinger);
                wakelock.release();
                return true;  //s 中断
            }
        }  //s 追加ここまで

        notificationManager.notify(NOTIFICATION_ID, notification);  //s 通知を配信

        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume, 0);
                audioManager.setRingerMode(currentRinger);
            }
        }, 3, TimeUnit.SECONDS);

        wakelock.release();
        return false;
    }

    /**
     * s 追加：振動パターンを返す
     * @param mode  通知の回答期限が過ぎたときの通知だったら false
     * @return 振動パターン
     */
    private long[] makeVibrationPattern(boolean mode) {
        long[][] vibrationPattern = {
                new long[] {
                        0,
                        100, 100, 100, 100, 100, 300,
                        100, 100, 100, 100, 100, 300,
                        100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100
                },     //3,3,7拍子  //s 元の固定パターン
                new long[] {
                        0,
                        200, 100, 100, 200, 100, 100,
                        200, 100, 100, 200, 100, 100,
                        100, 100, 100, 100, 100, 100, 100, 100, 50, 50, 100, 100, 100
                },  //s レアパターン
                new long[] {
                        0,
                        200, 100, 50, 50, 100, 100, 100, 100,
                        200, 100, 50, 50, 100, 100, 100, 100,
                        100, 100, 50, 50, 100, 100, 100, 100, 50, 50, 100, 100, 100, 100, 100
                }  //s 激レアパターン
        };
        long[] vibrationPatternSmall = {
                0,
                100
        };  //s 控えめに一応震えてみるパターン

        //ny 評価アンケートなので控えめな振動
        return vibrationPatternSmall;

        //ny 以下、以前のもの

        //s 通知の回答期限が過ぎたときの通知だったら 控えめパターンにする
//        if (!mode) {
//            return vibrationPatternSmall;
//        }
//
//        int pattern = vibrationPattern.length - 1;
//        double rnd = Math.random();
//
//        if (rnd < 0.97) {
//            pattern = 0;
//        } else if (rnd < 0.995) {
//            pattern = 1;
//        } else {
//            pattern = 2;
//        }
//
//        return vibrationPattern[pattern];
    }
}
