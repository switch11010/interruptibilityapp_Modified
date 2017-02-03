package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import java.util.HashMap;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.BoolData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.IntData;

/**
 * 状態変化のイベントを受け取るクラス
 * Created by hi on 2015/11/11.
 */
public class BroadcastData extends BroadcastReceiver implements DataReceiver {

    private Map<String, Data> data = new HashMap<>();
    private Context context;

    public BroadcastData(Context context){
        this.context = context;

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, batteryFilter);

        int status = 0;
        if (batteryStatus != null) {
            status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        }

        //noinspection deprecation
        data.put(HEADSET_PLUG, new BoolData(hasHeadset(context)));
        data.put(POWER_CONNECTED, new BoolData(status == BatteryManager.BATTERY_STATUS_CHARGING ||  status == BatteryManager.BATTERY_STATUS_FULL));
        data.put(SCREEN_ON, new BoolData(isActive(context)));
        data.put(RINGER_MODE, new IntData(getRingerMode(context)));

        IntentFilter filter =  new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        context.registerReceiver(this, filter);
    }

    @Override
    public Map<String, Data> getData() {
        return data;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action != null) {
            BoolData val;
            switch (action) {
                case Intent.ACTION_SCREEN_ON:
                    val = (BoolData) data.get(SCREEN_ON);
                    val.value = isActive(context);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    val = (BoolData) data.get(SCREEN_ON);
                    val.value = isActive(context);
                    break;
                case Intent.ACTION_POWER_CONNECTED:
                    val = (BoolData) data.get(POWER_CONNECTED);
                    val.value = true;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    val = (BoolData) data.get(POWER_CONNECTED);
                    val.value = false;
                    break;
                case Intent.ACTION_HEADSET_PLUG: {
                    val = (BoolData) data.get(HEADSET_PLUG);
                    val.value = hasHeadset(context);
                    break;
                }
                case AudioManager.RINGER_MODE_CHANGED_ACTION: {
                    IntData d = (IntData) data.get(RINGER_MODE);
                    d.value = getRingerMode(context);
                    break;
                }
            }
        }
    }

    public void release(){
        context.unregisterReceiver(this);
    }

    private static int getRingerMode(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getRingerMode();
    }

    private static boolean hasHeadset(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //noinspection deprecation
        return audioManager.isWiredHeadsetOn();
    }

    private static boolean isActive(Context context){
        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
            return powerManager.isInteractive();
        }
        //noinspection deprecation
        return powerManager.isScreenOn();
    }
}
