package ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ac.tuat.fujitaken.kk.test.testapplication.R;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data.ListItem;

/**
 * Created by hi on 2015/06/26.
 */
public class WifiAdapter extends ArrayAdapter {

    private LayoutInflater layoutInflater;

    public WifiAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ListItem item = (ListItem) getItem(position);

        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.list_wifiap_item, null);
        }

        TextView ssid = (TextView)convertView.findViewById(R.id.txtSSID);
        TextView bssid = (TextView)convertView.findViewById(R.id.txtBSSID);
        TextView level = (TextView)convertView.findViewById(R.id.txtLevel);
        TextView count = (TextView)convertView.findViewById(R.id.txtSampleCount);
        TextView freq = (TextView)convertView.findViewById(R.id.txt_frequency);

        ssid.setText(item.ap.ssid);
        bssid.setText(item.ap.mac);
        level.setText(String.format("%.2f", item.pattern.averageLevel));
        count.setText(String.valueOf(item.pattern.sampleCount));
        freq.setText(String.format("%.1f", ((float)item.ap.frequency)/1000));

        return convertView;
    }
}
