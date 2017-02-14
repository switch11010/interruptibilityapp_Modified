package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashMap;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.IntData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.StringData;

/**
 * UIイベントを受け取るクラス
 * Created by hi on 2015/11/10.
 */
class AccessibilityData implements DataReceiver {

    @SuppressLint("UseSparseArrays")
    private Map<Integer, Data> current = new HashMap<>(),
            latest = new HashMap<>();

    private PackageManager packageManager;

    AccessibilityData(Context context){
        packageManager = context.getPackageManager();

        Map<Integer, String> names = getNames();
        for(Map.Entry<Integer, String> entry: names.entrySet()){
            int key = entry.getKey();
            if(key == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
                current.put(key, new StringData(""));
                latest.put(key, new StringData(""));
            }
            else {
                current.put(key, new IntData(0));
                latest.put(key, new IntData(0));
            }
        }
    }

    private Map<Integer, String> getNames(){
        @SuppressLint("UseSparseArrays") Map<Integer, String> names = new HashMap<>();
        names.put(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED, NOTIFICATION);
        names.put(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, WINDOW_STATE_CHANGED);
        names.put(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED, WINDOW_CONTENT_CHANGED);
        names.put(AccessibilityEvent.TYPE_VIEW_FOCUSED, VIEW_FOCUSED);
        names.put(AccessibilityEvent.TYPE_VIEW_SELECTED, VIEW_SELECTED);
        names.put(AccessibilityEvent.TYPE_VIEW_CLICKED, VIEW_CLICKED);
        names.put(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED, VIEW_LONG_CLICKED);
        names.put(AccessibilityEvent.TYPE_VIEW_SCROLLED, VIEW_SCROLLED);
        names.put(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED, VIEW_TEXT_CHANGED);
        names.put(AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED, VIEW_TEXT_SELECTION_CHANGED);
        return names;
    }

    /**
     * データの更新
     * @param event 更新するイベント
     */
    public void put(AccessibilityEvent event){
        int type = event.getEventType();
        if(type == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
            StringData val = (StringData) current.get(type);
            String notifyApp;
            Notification notification = (Notification)event.getParcelableData();
            if (notification == null || notification.when <= System.currentTimeMillis() - Constants.APP_TIME_LIMITATION) {
                return;
            }
            if (notification.sound == null && notification.vibrate == null) {
                return;
            }
            try {
                CharSequence packageName = event.getPackageName();
                PackageInfo activityInfo = packageManager.getPackageInfo(packageName.toString(), PackageManager.GET_ACTIVITIES);
                CharSequence appLabel = activityInfo.applicationInfo.loadLabel(packageManager);
                notifyApp = appLabel.toString();
                notifyApp = notifyApp.replace(",", "，");
                notifyApp = notifyApp.replace("\n", "，");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                notifyApp = "";

            }
            Log.d("NOTIFY", notifyApp);
            val.value = notifyApp;
        }
        else {
            IntData val = (IntData) current.get(type);
            if (val != null) {
                val.value++;
            }
        }
    }

    @Override
    public Map<String, Data> getData() {
        Map<String, Data> data = new HashMap<>();
        Map<Integer, String> names = getNames();
        for(Map.Entry<Integer, String> entry: names.entrySet()) {
            data.put(entry.getValue(), this.latest.get(entry.getKey()));
        }
        return data;
    }

    void refresh() {
        for(Map.Entry<Integer, Data> entry: current.entrySet()){
            Data data = entry.getValue();
            Data temp = latest.get(entry.getKey());
            if(entry.getKey() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
                StringData val = ((StringData)data);
                ((StringData) temp).value = val.value;
                val.value = "";
            }
            else {
                IntData val = ((IntData)data);
                ((IntData) temp).value = val.value;
                ((IntData)data).value = 0;
            }
        }
    }
}