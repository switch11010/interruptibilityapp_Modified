package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;

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

    public BroadcastData(Context c){
        this.context = c;

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = c.registerReceiver(null, batteryFilter);

        int status = 0;
        if (batteryStatus != null) {
            status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        }

        //noinspection deprecation
        data.put(HEADSET_PLUG, new BoolData(hasHeadset(c)));
        data.put(POWER_CONNECTED, new BoolData(status == BatteryManager.BATTERY_STATUS_CHARGING ||  status == BatteryManager.BATTERY_STATUS_FULL));
        data.put(SCREEN_ON, new BoolData(true));
        data.put(UNLOCKED, new BoolData(true));  //s 追加
        data.put(RINGER_MODE, new IntData(getRingerMode(c)));

        IntentFilter filter =  new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);  //s 追加
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        c.registerReceiver(this, filter);
    }

    @Override
    //s DataReciever からの implements
    public Map<String, Data> getData() {
        return data;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        String action = intent.getAction();

        if(action != null) {
            BoolData val;
            switch (action) {
                case Intent.ACTION_SCREEN_ON:
                    val = (BoolData) data.get(SCREEN_ON);
                    val.value = true;
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    val = (BoolData) data.get(SCREEN_ON);
                    val.value = false;
                    val = (BoolData) data.get(UNLOCKED);  //s 追加
                    val.value = false;  //s 追加
                    break;
                case Intent.ACTION_USER_PRESENT:  //s 追加
                    val = (BoolData) data.get(UNLOCKED);
                    val.value = true;
                    break;  //s 追加ここまで
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
                    val.value = hasHeadset(c);
                    break;
                }
                case AudioManager.RINGER_MODE_CHANGED_ACTION: {
                    IntData d = (IntData) data.get(RINGER_MODE);
                    d.value = getRingerMode(c);
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
}
