package ac.tuat.fujitaken.exp.interruptibilityapp.data.base;

/**
 * センサーデータを保持するデータ型
 * float[]でデータを保持する
 */
public class SensorData extends Data {

    public float[] values;

    public SensorData(float[] values){
        this.values = values;
    }

    @Override
    public String getString() {
        StringBuilder builder = new StringBuilder();
        for(float v: values) {
            builder.append(v).append(",");
        }
        return builder.substring(0, builder.length()-1);
    }
}
