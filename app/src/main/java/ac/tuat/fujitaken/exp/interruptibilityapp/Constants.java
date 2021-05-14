package ac.tuat.fujitaken.exp.interruptibilityapp;

/**
 *
 * Created by hi on 2017/01/18.
 */

public interface Constants {
    int MAIN_LOOP_PERIOD = 500,     //メインスレッドのループ間隔（ms）
            ACC_LOOP_PERIOD = 20,   //加速度用スレッドのループ間隔（ms）
            SAVE_LOOP_PERIOD = 3,   //データ保存用スレッドのループ間隔（m）

            NOTIFICATION_THRESHOLD =  30 * 1000, //通知遷移と判断するまで時間（ms）  //s これを超えると通知とは関係ない遷移だと判断される

            NOTIFICATION_SLEEP = 5 * 1000,  //最低通知間隔（ms）

            NOTIFICATION_INTERVAL = 7 * 60 * 1000,  //最低通知間隔（ms）
            STORAGE_FREE_SPACE_LIMITATION = 100*1000*1000,  //ログ保存のために最低確保するストレージサイズ
            APP_TIME_LIMITATION = 60 * 60 * 1000,    //アプリ使用履歴を遡って参照する最大時間
            DEFAULT_PORT = 8890,       //通信ポート番号の初期値
            DEFAULT_SP_ID = 10,         //通信相手IDの初期値
            PC_OPERATION_LIMITATION = 60 * 1000,    //PC作業と判断するための時間
            SCREEN_OFF_TIME = 30 * 1000,
            UDP_TIMEOUT = 150;

    double WALK_THRESHOLD = 1.5,    //歩行開始判定までの時間（s）
            NOT_WALK_THRESHOLD = 5; //歩行終了判定までの時間（s）
    String POSITION_ARG = "position_arg";
}
