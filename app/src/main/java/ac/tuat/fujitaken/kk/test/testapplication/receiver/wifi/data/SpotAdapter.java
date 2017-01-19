package ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ac.tuat.fujitaken.kk.test.testapplication.R;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data.Spot;


/**
 * Created by seuo on 15/06/30.
 */
public class SpotAdapter extends ArrayAdapter {

    private LayoutInflater layoutInflater;

    public SpotAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Spot spot = (Spot) getItem(position);

        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.spot_layout, null);
        }

        TextView text = (TextView)convertView.findViewById(R.id.spot);
        TextView num = (TextView)convertView.findViewById(R.id.number);
        text.setText(spot.name);
        num.setText(String.valueOf(spot.id));

        return convertView;
    }
}