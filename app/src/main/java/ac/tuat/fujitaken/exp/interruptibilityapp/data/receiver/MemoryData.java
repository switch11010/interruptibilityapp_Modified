package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.IntData;

import static android.content.Context.ACTIVITY_SERVICE;


class MemoryData implements DataReceiver {
    private IntData totalmemory; //トータルの空きメモリ容量
    private Context context;
    Map<String, Data> data = new HashMap<>();

    public MemoryData(Context context){
        this.context = context;
        totalmemory = new IntData(0);
        data.put(TOTAL_MEMORY, totalmemory);
    }

    public Map<String, Data> getData() {
        return data;
    }

    public void setTotalmemory() {
        ActivityManager activityManager = (ActivityManager)this.context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        this.totalmemory.value = (int)(memoryInfo.availMem/1024/1024);
//        LogEx.d("memory",String.valueOf(this.totalmemory.value));
        data.put(TOTAL_MEMORY, this.totalmemory);
    }
}
