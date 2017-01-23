package ac.tuat.fujitaken.exp.interruptibilityapp.loop;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * イベントを監視するクラス
 * Created by hi on 2015/11/11.
 */
public class RegularThread {

    private ScheduledExecutorService schedule = null;
    private List<ThreadListener> listeners = new ArrayList<>();
    private Runnable repeatTask = new Runnable() {
        @Override
        public void run() {
            for(int i = 0; i < listeners.size(); i++) {
                ThreadListener listener = listeners.get(i);
                if(listener == null){
                    listeners.remove(i--);
                }
                else{
                    listener.run();
                }
            }
        }
    };

    /**
     * 監視スタート
     */
    public void start(int delay, TimeUnit unit){
        if(schedule == null) {
            schedule = Executors.newSingleThreadScheduledExecutor();
            schedule.scheduleAtFixedRate(repeatTask, delay, delay, unit);
        }
    }

    public void setListener(ThreadListener listener){
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

    public interface ThreadListener {
        void run();
    }
}
