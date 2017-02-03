package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.HashMap;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.IntData;

/**
 * 電話の着信状態
 * Created by hi on 2015/11/27.
 */
public class PhoneState extends PhoneStateListener implements DataReceiver {

    private TelephonyManager telephonyManager;
    private IntData phoneState = new IntData(TelephonyManager.CALL_STATE_IDLE);

    public PhoneState(Context context){
        telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(this, LISTEN_CALL_STATE);
    }

    public void release(){
        telephonyManager.listen(this, LISTEN_NONE);
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        this.phoneState.value = state;
    }

    @Override
    public Map<String, Data> getData() {
        Map<String, Data> data = new HashMap<>();
        data.put(PHONE, phoneState);
        return data;
    }

    public int getValue(){
        return phoneState.value;
    }
}
