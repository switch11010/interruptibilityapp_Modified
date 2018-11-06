package ac.tuat.fujitaken.exp.interruptibilityapp.data.status;

import android.content.Context;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.R;

/**
 * 通知の有無
 * Created by hi on 2017/01/20.
 */

public class Notify {
    public static final int NOTIFICATION = 1 << 5;
    private String appName = "";
    private static final int NOTE_TIME = Constants.NOTIFICATION_THRESHOLD / Constants.MAIN_LOOP_PERIOD;  //s 通知による遷移だと判断するための閾値ループ回数
    private int noteCount = NOTE_TIME;

    public Notify(Context context){
        appName = context.getString(R.string.app_name);
    }

    //s 通知が配信されてから一定時間内かどうかを判定をする
    //s interruptTiming.run() から定期的に呼ばれる：引数で渡されるのは たぶん 最新の通知？（よくわからない）
    public int judge(String note){
        noteCount = (!"".equals(note) && !appName.equals(note))? 0: noteCount + 1;
        if(noteCount < NOTE_TIME){
            return NOTIFICATION;
        }
        return 0;  //s 通知が配信されていないか、時間が経っている
    }
}
