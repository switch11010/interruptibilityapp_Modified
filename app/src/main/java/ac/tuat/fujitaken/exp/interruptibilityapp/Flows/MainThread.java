package ac.tuat.fujitaken.exp.interruptibilityapp.Flows;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.AllData;

/**
 * Created by hi on 2017/04/04.
 */

public class MainThread implements Runnable {
    private ScheduledExecutorService schedule = null;
    private AllData mAllData;

    public MainThread(AllData allData){
        mAllData = allData;
    }

    public void start(){
        if(schedule == null) {
            schedule = Executors.newSingleThreadScheduledExecutor();
            schedule.scheduleAtFixedRate(this, Constants.MAIN_LOOP_PERIOD, Constants.MAIN_LOOP_PERIOD, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void run() {

    }

    /**
     * 監視ストップ
     */
    /*
    public void stop(){
        if(schedule != null) {
            schedule.shutdown();
            schedule = null;
        }
    }

    @Override
    public void run() {
        final RowData line = mAllData.newLine();
        Map<String, Data> map = mAllData.getData();

        int w = walking.judge(((BoolData)map.get(DataReceiver.WALK)).value);
        int s = screen.judge(mAllData.getData());
        int n = notify.judge(((StringData)map.get(DataReceiver.NOTIFICATION)).value);

        final int eventFlag = w | s | n;

        EvaluationData evaluationData = new EvaluationData();
        evaluationData.setValue(line);
        notificationController.save(evaluationData);

        if((eventFlag & (Walking.WALK_START | Walking.WALK_STOP | Screen.SCREEN_ON | Screen.SCREEN_OFF)) > 0) {
            Log.d("EVENT_COUNTER", String.valueOf(eventFlag));
            eventTriggeredThread(eventFlag, evaluationData);
            mAllData.scan();
        }
    }
    */
}
