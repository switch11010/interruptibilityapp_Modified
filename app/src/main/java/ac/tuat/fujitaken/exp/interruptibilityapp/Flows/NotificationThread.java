package ac.tuat.fujitaken.exp.interruptibilityapp.Flows;

/**
 * Created by hi on 2017/02/07.
 */

public class NotificationThread {
    /*
    public void start(){
        int event = eventFlag;
        boolean eval = false,
                noteFlag = note    //通知モード
                        && !NotificationController.hasNotification,  //待機状態の通知なし
                udpComm = (eventFlag & Screen.SCREEN_ON) > 0 || (eventFlag & Walking.WALK_START) > 0;   //UDP通信が必要かどうか

        String message = "null";
        if (udpConnection != null && udpComm) {

            udpConnection.sendRequest(line);
            message = udpConnection.receiveData();
            Log.d("UDP", "Received Time : " + (System.currentTimeMillis() - line.time));
        }
        event |= pc.judge(message);

        Log.d("EVENT", "Num is " + Integer.toBinaryString(event));
        Log.d("EVENT", notificationController.counter.getEventName(event));

        if (noteFlag) {
            double p = calcP(event);
            Log.d("P", "P is " + p);
            if (p >= 2  //評価数が他より1/2以下では時間に関係なく通知
                    ||( Math.random() < p && line.time - prevTime > Constants.NOTIFICATION_INTERVAL)) {    //前の通知から一定時間経過
                notificationController.normalNotify(event, line);
                eval = true;
            }
        }
        if(!eval){
            notificationController.saveEvent(event, line);
        }
    }

    private double calcP(int event){
        /**
         * 電話以外は確率を求めてから通知
         * ただし，評価数が平均の2倍or1/2の場合は補正
         */
    /*
        if(counter.getEvaluations(event) == null){
            return 0;
        }

        int min = counter.getEvaluationMin();
        min = min == 0? 1: min;
        int c = counter.getEvaluations(event);
        c = c == 0? 1: c;
        double p = min / c;
        double mean = counter.getEvaluationMean();

        if (mean > 1 && c >= mean + 10) {
            return 0;
        }

        double t = 0;
        if((event & Walking.WALK_START) > 0){
            t = counter.getEvaluations(event ^ (Walking.WALK_START | Walking.WALK_STOP));
        }
        else if((event & Screen.SCREEN_ON) > 0){
            t = counter.getEvaluations(event ^ (Screen.SCREEN_ON | Screen.SCREEN_OFF));
        }
        if (t != 0) {
            p /= 1 - min / t;
        }
        return p;
    }
    */
    /*
    * NotificationControllerの機能実装
    * */
}
