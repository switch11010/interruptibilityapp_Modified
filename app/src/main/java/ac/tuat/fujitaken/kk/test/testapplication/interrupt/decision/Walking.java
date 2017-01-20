package ac.tuat.fujitaken.kk.test.testapplication.interrupt.decision;


import ac.tuat.fujitaken.kk.test.testapplication.Constants;
import ac.tuat.fujitaken.kk.test.testapplication.interrupt.EventCounter;

/**
 * 歩行開始・終了検出
 * Created by hi on 2015/11/25.
 */
public class Walking {

    public static final int WALK_START = 1 << 1,
        WALK_STOP = 1 << 2;

    private final int
            NOT_WALK = 3,
            WALKING = 9;

    private int walkState = NOT_WALK,
            count = 0;

    public int judge(boolean v){
        switch (walkState){
            case WALKING:
                if(v){
                    count = 0;
                    return 0;
                }
                else if((++count >= Constants.NOT_WALK_THRESHOLD * 1000/Constants.MAIN_LOOP_PERIOD)){
                    count = 0;
                    walkState = NOT_WALK;
                    return WALK_START;
                }
                else{
                    return 0;
                }
            case NOT_WALK:
                if(v){
                    if(++count >= Constants.WALK_THRESHOLD * 1000/Constants.MAIN_LOOP_PERIOD){
                        count = 0;
                        walkState = WALKING;
                        return WALK_STOP;
                    }
                }
                else {
                    count = 0;
                }
        }
        return 0;
    }
}
