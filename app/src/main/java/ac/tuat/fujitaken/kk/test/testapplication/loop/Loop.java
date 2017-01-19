package ac.tuat.fujitaken.kk.test.testapplication.loop;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.kk.test.testapplication.Constants;

/**
 * イベントを監視するクラス
 * Created by hi on 2015/11/11.
 */
public class Loop {

    private ScheduledExecutorService schedule = null;
    protected List<LoopListener> listeners = new ArrayList<>();

    private Runnable repeatTask = new Runnable() {
        @Override
        public void run() {
            Loop.this.run();
        }
    };

    public void run(){
        for(int i = 0; i < listeners.size(); i++) {
            LoopListener listener = listeners.get(i);
            if(listener == null){
                listeners.remove(i);
                i--;
            }
            else{
                listener.onLoop(this);
            }
        }
    }

    /**
     * 監視スタート
     */
    public void start(int delay, TimeUnit unit){
        if(schedule == null) {
            schedule = Executors.newSingleThreadScheduledExecutor();
            schedule.scheduleAtFixedRate(repeatTask, delay, delay, unit);
        }
    }

    public void setListener(LoopListener listener){
        listeners.add(listener);
    }

    /**
     * 監視ストップ
     */
    public void stop(){
        if(schedule != null) {
            schedule.shutdown();
            schedule = null;
        }
    }

    public interface LoopListener {
        void onLoop(Loop loop);
    }
}
