package ac.tuat.fujitaken.kk.test.testapplication.interrupt.decision;

import android.content.Context;

import ac.tuat.fujitaken.kk.test.testapplication.Constants;
import ac.tuat.fujitaken.kk.test.testapplication.R;

/**
 * Created by hi on 2017/01/20.
 */

public class Notify {
    public static final int NOTIFICATION = 1 << 5;
    private String appName = "";
    private static final int NOTE_TIME = Constants.NOTIFICATION_THRESHOLD * 1000/Constants.MAIN_LOOP_PERIOD;
    private int noteCount = NOTE_TIME;

    public Notify(Context context){
        appName = context.getString(R.string.app_name);
    }

    public int judge(String note){
        noteCount = (!"".equals(note) && !appName.equals(note))? 0: noteCount + 1;
        if(noteCount < NOTE_TIME){
            return NOTIFICATION;
        }
        return 0;
    }
}
