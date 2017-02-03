package ac.tuat.fujitaken.exp.interruptibilityapp;

/**
 * Created by hi on 2017/01/18.
 */

public interface Constants {
    int MAIN_LOOP_PERIOD = 500,     //メインスレッドのループ間隔（ms）
            ACC_LOOP_PERIOD = 20,   //加速度用スレッドのループ間隔（ms）
            SAVE_LOOP_PERIOD = 3,   //データ保存用スレッドのループ間隔（m）

            NOTIFICATION_THRESHOLD = 30, //通知遷移と判断するまで時間（s）

            NOTIFICATION_INTERVAL = 7 * 60 * 1000;  //最低通知間隔（ms）

    double WALK_THRESHOLD = 1.5,    //歩行開始判定までの時間（s）
            NOT_WALK_THRESHOLD = 5; //歩行終了判定までの時間（s）
    String POSITION_ARG = "position_arg";
}
