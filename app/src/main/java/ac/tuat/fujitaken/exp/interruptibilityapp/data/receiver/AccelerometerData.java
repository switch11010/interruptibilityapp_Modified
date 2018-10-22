package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.apache.commons.math3.util.FastMath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.SensorData;

/**
 * 加速度を受け取る
 * Created by hi on 2015/11/19.
 */
public class AccelerometerData implements DataReceiver, SensorEventListener{

    private SensorData acc;  //s 最新のセンサデータが常に acc.value に格納されているっぽい　getData()で呼び出す
    private SensorManager manager;

    public AccelerometerData(Context context){
        this.manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensorList = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_FASTEST);
        acc = new SensorData(new float[]{0, 0, 0});
    }

    public RowData newLine(){
        RowData line = new RowData();
        line.data.add(acc.clone());
        return line;
    }

    public String getHeader(){
        return "ACCELEROMETER";
    }

    @Override
    //s DataReceiver からの implements
    //s private SensorData acc のゲッタ
    public Map<String, Data> getData() {
        Map<String, Data> data = new HashMap<>();
        data.put(ACCELEROMETER, acc);
        return data;
    }

    @Override
    //s センサの値が変更されると呼ばれる SensorEventListener の implements
    public void onSensorChanged(SensorEvent event) {
        acc.values = event.values.clone();
    }

    @Override
    //s センサの精度が変更されると呼ばれる SensorEventListener の implements
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    double getNorm(){
        double norm = 0;
        for(float v: acc.values){
            norm += FastMath.pow(v, 2);
        }
        return FastMath.sqrt(norm);
    }

    public void release(){
        manager.unregisterListener(this);
    }
}
