package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.SensorData;

/**
 * センサーデータを受け取るクラス
 */
class SensorReceiver implements DataReceiver, SensorEventListener{

    @SuppressLint("UseSparseArrays")
    private Map<Integer, Data> data = new HashMap<>();
    private SensorManager sensorManager;

    SensorReceiver(Context context){
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor s: sensorList){
            switch (s.getType()){
                case Sensor.TYPE_MAGNETIC_FIELD:
                case Sensor.TYPE_GYROSCOPE:
                    sensorManager.registerListener(this, s, 1000/ Constants.MAIN_LOOP_PERIOD);
                    data.put(s.getType(), new SensorData(new float[]{0, 0, 0}));
                    break;
                case Sensor.TYPE_PROXIMITY:
                case Sensor.TYPE_LIGHT:
                    sensorManager.registerListener(this, s, 1000/ Constants.MAIN_LOOP_PERIOD);
                    data.put(s.getType(), new SensorData(new float[]{0}));
                    break;
                default:
                    break;
            }
        }
    }

    public void release(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public Map<String, Data> getData() {
        Map<String, Data> newMap = new HashMap<>();
        newMap.put(MAGNETIC_FIELD, this.data.get(Sensor.TYPE_MAGNETIC_FIELD));
        newMap.put(GYROSCOPE, this.data.get(Sensor.TYPE_GYROSCOPE));
        newMap.put(PROXIMITY, this.data.get(Sensor.TYPE_PROXIMITY));
        newMap.put(LIGHT, this.data.get(Sensor.TYPE_LIGHT));
        return newMap;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        SensorData val;
        switch (type){
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_GYROSCOPE:
                val = (SensorData) data.get(type);
                val.values = event.values.clone();
                break;
            case Sensor.TYPE_PROXIMITY:
            case Sensor.TYPE_LIGHT:
                val = (SensorData) data.get(type);
                val.values = new float[]{event.values[0]};
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
