package ac.tuat.fujitaken.exp.interruptibilityapp.data.status;

import android.content.Context;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.BoolData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.IntData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.DataReceiver;

/**
 * 画面オン・オフの検出
 * Created by hi on 2015/11/25.
 */
public class Screen {

    public static final int SCREEN_ON = 1 << 3,
            SCREEN_OFF = 1 << 4;

    private boolean prevState = true;
    private List<Integer> buffer = new ArrayList<>();
    private int sumOps = 0;
    private boolean prevConnect = false;
    private String appName = "";

    public Screen(Context context){
        int screenOffTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30 * 1000) / Constants.MAIN_LOOP_PERIOD - 1;
        sumOps = 1;
        for(int i = 0; i < screenOffTimeout -1; i++){
            buffer.add(0);
        }
        buffer.add(1);
        appName = context.getString(R.string.app_name);
    }

    public int judge(Map<String, Data> data){

        boolean latestValue = ((BoolData)data.get(DataReceiver.SCREEN_ON)).value;
        boolean connect = ((BoolData)data.get(DataReceiver.POWER_CONNECTED)).value;
        String noteApp = ((StringData)data.get(DataReceiver.NOTIFICATION)).value;

        int ops = (
        ((IntData)data.get(DataReceiver.VIEW_CLICKED)).value > 0
                | ((IntData)data.get(DataReceiver.VIEW_LONG_CLICKED)).value > 0
                | ((IntData)data.get(DataReceiver.VIEW_SCROLLED)).value > 0
                | ((IntData)data.get(DataReceiver.VIEW_TEXT_CHANGED)).value > 0
                | ((IntData)data.get(DataReceiver.VIEW_TEXT_SELECTION_CHANGED)).value > 0
        )? 1: 0;
        sumOps -= buffer.remove(0);
        sumOps += ops;
        buffer.add(ops);

        boolean on = latestValue && !prevState && !(prevConnect && !connect) && !appName.equals(noteApp),
        off = !latestValue && prevState && sumOps > 0;
        if(on){
            sumOps -= buffer.remove(0);
            sumOps += 1;
            buffer.add(1);
        }

        prevConnect = connect;
        prevState = latestValue;
        int ret = 0;
        if(on){
            ret = SCREEN_ON;
        }
        else if(off){
            ret = SCREEN_OFF;
        }
        return ret;
    }
}