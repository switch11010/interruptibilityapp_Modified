package ac.tuat.fujitaken.exp.interruptibilityapp.interrupt.question;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.ui.fragments.SettingFragment;

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

    public static final String TYPE = "NOTIFICATION_TYPE",
                                NOTIFICATION_TAG = "INTERRUPTION_NOTIFICATION";

    private static final int REQUEST_CODE = 753,
                                NOTIFICATION_ID = 357;

    private NotificationManager notificationManager;
    private AudioManager audioManager;

    public InterruptionNotification(Context context){
        this.context = context;
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        volume = preferences.getInt(SettingFragment.VOLUME, audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));

        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void cancel(){
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void normalNotify(final Bundle bundle){
        notification(bundle, true);
    }

    public void cancelNotify(final Bundle bundle){
        notification(bundle, false);
    }

    private void notification(Bundle bundle, boolean mode){
        Intent intent = new Intent(context, QuestionActivity.class);
        bundle.putBoolean(TYPE, mode);
        intent.putExtras(bundle);
        PendingIntent pi = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("割り込み通知")
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(Integer.MAX_VALUE)
                .setVibrate(new long[]{0, 100, 100, 100, 100, 100, 300, 100, 100, 100, 100, 100, 300, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100})     //3,3,7拍子
                .setLights(Color.rgb(255, 215, 0), 3000, 3000)     //金色っぽい色
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setOnlyAlertOnce(true);

        if(mode){
            builder.setContentText("話しかけを受け、5分間の作業中断発生")
                    .setTicker("話しかけを受け、5分間の作業中断発生");
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
                | PowerManager.ON_AFTER_RELEASE, NOTIFICATION_TAG);
        wakelock.acquire();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        currentRinger = audioManager.getRingerMode();
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        if(volume == 0){
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
        else{
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
        }

        notificationManager.notify(NOTIFICATION_ID, notification);

        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume, 0);
                audioManager.setRingerMode(currentRinger);
            }
        }, 3, TimeUnit.SECONDS);

        wakelock.release();
    }
}
