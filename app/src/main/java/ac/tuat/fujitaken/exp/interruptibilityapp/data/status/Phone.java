package ac.tuat.fujitaken.exp.interruptibilityapp.data.status;

import android.telephony.TelephonyManager;

/**
 *
 * Created by hi on 2015/12/10.
 */
public class Phone {

    private int phoneState = TelephonyManager.CALL_STATE_IDLE;

    public int judge(int newState) {
        int ret = 0;
        if (phoneState == TelephonyManager.CALL_STATE_IDLE && newState == TelephonyManager.CALL_STATE_RINGING) {
            ret = 1<<25;

        } else if (phoneState == TelephonyManager.CALL_STATE_OFFHOOK && newState== TelephonyManager.CALL_STATE_IDLE) {
            ret = 1<<26;
        }
        phoneState = newState;
        return ret;
    }
}
