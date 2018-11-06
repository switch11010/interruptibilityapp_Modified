package ac.tuat.fujitaken.exp.interruptibilityapp.flow;


import android.util.Log;

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
    private Runnable repeatTask = ()->{
        for(int i = 0; i < listeners.size(); i++) {
            ThreadListener listener = listeners.get(i);  //s ThreadListener：下にある interface
            if(listener == null){
                listeners.remove(i--);
            }
            else{
                listener.run();
            }
        }
    };  //s 下の start() の実行でこれがスケジューラに登録されて repeatTask.run() が定期実行され、登録されたクラスの run() が順に実行される

    /**
     * 監視スタート
     */
    public void start(int delay, TimeUnit unit){
        if(schedule == null) {
            schedule = Executors.newSingleThreadScheduledExecutor();
            schedule.scheduleAtFixedRate(repeatTask, delay, delay, unit);  //s delayおきにrepeatTask処理を開始する（処理開始直後に待機開始）
            //Log.d("EVENT", listeners.size() + "");  //s コメントアウト：変更前
            Log.d("RegularThread.start", "リスナー登録数：" + listeners.size());  //s 変更
        }
    }

    //s 下の RegularThread.ThreadListener を implements したインスタンスを このメソッドを使って登録しとくと
    //s ThreadListener.run() が 定期的に実行されるらしい
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

    //s run() の内容を定義して、定期的に実行させる
    public interface ThreadListener {
        void run();
    }
}
