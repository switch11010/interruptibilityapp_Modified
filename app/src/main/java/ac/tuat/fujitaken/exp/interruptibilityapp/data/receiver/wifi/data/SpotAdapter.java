package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;


/**
 *
 * Created by seuo on 15/06/30.
 */
public class SpotAdapter extends ArrayAdapter {

    private LayoutInflater layoutInflater;

    public SpotAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        Spot spot = (Spot) getItem(position);

        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.spot_layout, parent);
        }

        TextView text = (TextView)convertView.findViewById(R.id.spot);
        TextView num = (TextView)convertView.findViewById(R.id.number);
        if(spot != null) {
            text.setText(spot.name);
            num.setText(String.valueOf(spot.id));
        }

        return convertView;
    }
}