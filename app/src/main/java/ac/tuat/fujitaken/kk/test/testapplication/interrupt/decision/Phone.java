package ac.tuat.fujitaken.kk.test.testapplication.interrupt.decision;

import android.telephony.TelephonyManager;

import ac.tuat.fujitaken.kk.test.testapplication.interrupt.EventCounter;

/**
 *
 * Created by hi on 2015/12/10.
 */
public class Phone {

    private int phoneState = TelephonyManager.CALL_STATE_IDLE;

    public int judge(int newState) {
        int ret = 0;
        if (phoneState == TelephonyManager.CALL_STATE_IDLE && newState == TelephonyManager.CALL_STATE_RINGING) {
            ret = EventCounter.PHONE_START_FLAG;

        } else if (phoneState == TelephonyManager.CALL_STATE_OFFHOOK && newState== TelephonyManager.CALL_STATE_IDLE) {
            ret = EventCounter.PHONE_STOP_FLAG;
        }
        phoneState = newState;
        return ret;
    }
}
