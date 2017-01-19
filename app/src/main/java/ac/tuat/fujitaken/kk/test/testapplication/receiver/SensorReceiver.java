package ac.tuat.fujitaken.kk.test.testapplication.receiver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.tuat.fujitaken.kk.test.testapplication.data.Data;
import ac.tuat.fujitaken.kk.test.testapplication.data.SensorData;

/**
 * センサーデータを受け取るクラス
 */
public class SensorReceiver implements DataReceiver, SensorEventListener{

    private Map<Integer, Data> data = new HashMap<>();
    private SensorManager sensorManager;

    public SensorReceiver(Context context){
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor s: sensorList){
            switch (s.getType()){
                case Sensor.TYPE_MAGNETIC_FIELD:
                case Sensor.TYPE_GYROSCOPE:
                    sensorManager.registerListener(this, s, 500 * 1000);
                    data.put(s.getType(), new SensorData(new float[]{0, 0, 0}));
                    break;
                case Sensor.TYPE_PROXIMITY:
                case Sensor.TYPE_LIGHT:
                    sensorManager.registerListener(this, s, 500 * 1000);
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
        Map<String, Data> data = new HashMap<>();
        data.put(MAGNETIC_FIELD, this.data.get(Sensor.TYPE_MAGNETIC_FIELD));
        data.put(GYROSCOPE, this.data.get(Sensor.TYPE_GYROSCOPE));
        data.put(PROXIMITY, this.data.get(Sensor.TYPE_PROXIMITY));
        data.put(LIGHT, this.data.get(Sensor.TYPE_LIGHT));
        return data;
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
