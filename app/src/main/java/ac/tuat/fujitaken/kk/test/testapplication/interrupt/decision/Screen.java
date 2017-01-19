package ac.tuat.fujitaken.kk.test.testapplication.interrupt.decision;

import android.content.Context;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.kk.test.testapplication.Constants;
import ac.tuat.fujitaken.kk.test.testapplication.R;
import ac.tuat.fujitaken.kk.test.testapplication.data.BoolData;
import ac.tuat.fujitaken.kk.test.testapplication.data.Data;
import ac.tuat.fujitaken.kk.test.testapplication.data.IntData;
import ac.tuat.fujitaken.kk.test.testapplication.data.StringData;
import ac.tuat.fujitaken.kk.test.testapplication.interrupt.EventCounter;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.DataReceiver;

/**
 * 画面オン・オフの検出
 * Created by hi on 2015/11/25.
 */
public class Screen {

    private boolean prevState = true;
    private List<Integer> buffer = new ArrayList<>();
    private int sumOps = 0;
    private String appName;
    private boolean prevConnect = false;
    private int noteCount = Constants.NOTIFICATION_THRESHOLD * 1000/Constants.MAIN_LOOP_PERIOD;
    private String prevOn = "";
    private String prevNote = "";

    public Screen(Context context){
        int screenOffTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30 * 1000) * Constants.MAIN_LOOP_PERIOD - 1;
        sumOps = 1;
        for(int i = 0; i < screenOffTimeout -1; i++){
            buffer.add(0);
        }
        buffer.add(1);

        appName = context.getString(R.string.app_name);
    }

    public int judge(Map<String, Data> data){

        boolean latestValue = ((BoolData)data.get(DataReceiver.SCREEN_ON)).value;
        String noteApp = ((StringData)data.get(DataReceiver.NOTIFICATION)).value;
        boolean connect = ((BoolData)data.get(DataReceiver.POWER_CONNECTED)).value;

        boolean note = !"".equals(noteApp);
        if(note){
            prevNote = noteApp;
            noteCount = 0;
        }
        noteCount++;

        int ops = 0;
        ops += ((IntData)data.get(DataReceiver.VIEW_CLICKED)).value;
        ops += ((IntData)data.get(DataReceiver.VIEW_LONG_CLICKED)).value;
        ops += ((IntData)data.get(DataReceiver.VIEW_SCROLLED)).value;
        ops += ((IntData)data.get(DataReceiver.VIEW_TEXT_CHANGED)).value;
        sumOps -= buffer.remove(0);
        sumOps += ops;
        buffer.add(ops);

        int ret = 0;

        if(latestValue && !prevState) {
            //画面オン
            //30秒以内に通知があったら，通知による遷移
            if(noteCount < Constants.NOTIFICATION_THRESHOLD * 1000/Constants.MAIN_LOOP_PERIOD){
                noteCount = Constants.NOTIFICATION_THRESHOLD * 1000/Constants.MAIN_LOOP_PERIOD;
                //自分のアプリの通知だったら無視
                if(appName.equals(prevNote)) {
                    prevOn = "";
                    prevNote = "";
                }
                else{
                    prevOn = EventCounter.NOTIFICATION;
                    ret = EventCounter.NOTIFICATION_FLAG;
                }
            }
            else if(!(prevConnect && !connect)){
                prevOn = EventCounter.SELF_SCREEN_ON;
                ret =  EventCounter.SELF_SCREEN_ON_FLAG;
            }
        }
        else if(!latestValue && prevState){
            //画面オフ
            noteCount = Constants.NOTIFICATION_THRESHOLD * 1000/Constants.MAIN_LOOP_PERIOD;
            if(sumOps > 0) {
                switch (prevOn) {
                    case EventCounter.NOTIFICATION:
                        ret = EventCounter.EXT_SCREEN_OFF_FLAG;
                        break;
                    case EventCounter.SELF_SCREEN_ON:
                        ret = EventCounter.SELF_SCREEN_OFF_FLAG;
                        break;
                }
            }
        }
        prevConnect = connect;
        prevState = latestValue;
        return ret;
    }
}