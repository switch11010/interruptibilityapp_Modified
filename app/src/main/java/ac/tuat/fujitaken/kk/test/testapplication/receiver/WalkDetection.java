package ac.tuat.fujitaken.kk.test.testapplication.receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.kk.test.testapplication.Constants;
import ac.tuat.fujitaken.kk.test.testapplication.data.BoolData;
import ac.tuat.fujitaken.kk.test.testapplication.data.Data;
import ac.tuat.fujitaken.kk.test.testapplication.loop.RegularThread;

/**
 * 歩行検出用
 * Created by hi on 2015/11/12.
 */
public class WalkDetection implements DataReceiver, RegularThread.ThreadListener {

    private static final double _1G = 9.8;

    private static final int up = 1,
            below = 2,
            stable = 3,

            sampleIn4S = 200,      //4秒分のサンプル数
            correlationSize = 100,  //自己相関のサイズ
            lagMin = 25,    //自己相関のラグの最小値
            lagMax = 100,    //自己相関のラグの最大値
            lagAdd = 5;     //自己相関のラグの間隔

    private double[] max = new double[1000/Constants.MAIN_LOOP_PERIOD],
            min = new double[1000/Constants.MAIN_LOOP_PERIOD],
            prevMax = new double[1000/Constants.MAIN_LOOP_PERIOD],
            prevMin = new double[1000/Constants.MAIN_LOOP_PERIOD],
            cross = new double[1000/Constants.MAIN_LOOP_PERIOD],
            prevCross = new double[1000/Constants.MAIN_LOOP_PERIOD];

    private int crossState = stable,
            count = 0,
            m = 0;

    private volatile List<Double> buffer = new ArrayList<>();

    private BoolData val = new BoolData(false);
    private AccelerometerData acc;

    public WalkDetection(AccelerometerData acc){
        this.acc = acc;
        for(int i = 0; i < sampleIn4S; i++){
            buffer.add(_1G);
        }
        for(int i = 0; i < 1000/Constants.MAIN_LOOP_PERIOD; i++){
            max[i] = _1G;
            min[i] = _1G;
            cross[i] = 0;
            prevMax[i] = _1G;
            prevMin[i] = _1G;
            prevCross[i] = 0;
        }
    }

    public boolean getValue(){
        return val.value;
    }

    @Override
    public Map<String, Data> getData() {
        Map<String, Data> data = new HashMap<>();
        data.put(WALK, val);
        return data;
    }

    public boolean judge(){
        double maxDiff = (prevMax[m] - _1G)/(max[m] - _1G),
                minDiff = (_1G - prevMin[m])/(_1G - min[m]),
                crossDiff = prevCross[m]/cross[m];
        if(val.value){
            walkingJudge(maxDiff, minDiff, crossDiff);
        }
        else{
            normalJudge(maxDiff, minDiff, crossDiff);
        }
        refresh();
        return val.value;
    }

    private void normalJudge(double maxDiff, double minDiff, double crossDiff){
        if(cross[m] >= 2 && cross[m] <= 11){
            if(maxDiff >= 0.6 && maxDiff <= 1.9 &&
                    minDiff >= 0.6 && minDiff <= 1.9 &&
                    crossDiff >= 0.5 && crossDiff <= 2) {
                val.value = correlation() > 0.4;
                return;
            }
        }
        val.value = false;
    }

    private void walkingJudge(double maxDiff, double minDiff, double crossDiff){
        if(cross[m] >= 2 && cross[m] <= 14){
            if(maxDiff >= 0.4 && maxDiff <= 2.5 &&
                    minDiff >= 0.4 && minDiff <= 2.5 &&
                    crossDiff >= 0.3 && crossDiff <= 3){
                val.value = correlation() > 0.2;
                return;
            }
        }
        val.value = false;
    }

    private void refresh(){
        prevMax[m] = max[m];
        prevMin[m] = min[m];
        prevCross[m] = cross[m];
        cross[m] = 0;
        min[m] = _1G;
        max[m] = _1G;
        m = (m+1)% (1000/Constants.MAIN_LOOP_PERIOD);
    }

    private void gCross(double value) {
        double belowThreshold = _1G - 1,
                upperThreshold = _1G + 1;
        if (crossState != stable) {
            count++;
            switch (crossState) {
                case up:
                    if (value < belowThreshold) {
                        crossState = below;
                        addCross();
                        count = 0;
                    }
                    break;
                case below:
                    if (value > upperThreshold) {
                        crossState = up;
                        addCross();
                        count = 0;
                    }
                    break;
            }
            if (count >= sampleIn4S) {
                crossState = stable;
                count = 0;
            }
        }
        else {
            if (value > upperThreshold) {
                crossState = up;
            }
            else if (value < belowThreshold) {
                crossState = below;
            }
        }
    }

    private void addCross(){
        for(int i = 0; i < 1000/Constants.MAIN_LOOP_PERIOD; i++){
            cross[i]++;
        }
    }

    private double correlation(){
        List<Double> temp = new ArrayList<>(buffer);
        List<Double> list1 = new ArrayList<>();
        int lag = lagMin-lagAdd;
        double maxC = -1,
                mean1 = 0,
                dis1 = 0,
                sum = 0;

        //共通するデータの計算を先にする
        //ずらさないほうの波形の正規化と，ずらすほうの平均値の初期化
        for(int i = 0; i < correlationSize; i++){
            mean1 += temp.get(i);
            sum += temp.get(i + lag);
        }
        mean1 /= correlationSize;
        for(int i = 0; i < correlationSize; i++){
            list1.add(temp.get(i) - mean1);
            dis1 += Math.pow(temp.get(i) - mean1, 2);
        }
        dis1 = Math.sqrt(dis1);

        //タイムフレームをずらしながら相関を計算
        while(lag <= lagMax-lagAdd){
            for(int i = 0; i < lagAdd; i++){
                sum -= temp.get(lag);
                sum += temp.get(lag+correlationSize);
                lag++;
            }
            double mean2 = sum/correlationSize;
            double dis2 = 0;
            double correlation = 0;
            for(int i = 0; i < correlationSize; i++){
                correlation += list1.get(i)*(temp.get(i + lag) - mean2);
                dis2 += Math.pow(temp.get(i + lag) - mean2, 2);
            }
            correlation /= Math.sqrt(dis2)*dis1;
            if(correlation > maxC){
                maxC = correlation;
            }
        }
        return maxC;
    }

    @Override
    public void run() {
        double norm = acc.getNorm();
        buffer.add(norm);
        buffer.remove(0);
        gCross(norm);
        for(int i = 0; i < 1000/Constants.MAIN_LOOP_PERIOD; i++) {
            max[i] = norm > max[i] ? norm : max[i];
            min[i] = norm < min[i] ? norm : min[i];
        }
    }
}
