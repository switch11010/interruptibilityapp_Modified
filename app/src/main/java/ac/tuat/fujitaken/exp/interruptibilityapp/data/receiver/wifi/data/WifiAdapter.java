package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Locale;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;

/**
 * AP表示用アダプタ
 * Created by hi on 2015/06/26.
 */
public class WifiAdapter extends ArrayAdapter {

    private LayoutInflater layoutInflater;

    public WifiAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ListItem item = (ListItem) getItem(position);

        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.list_wifiap_item, parent);
        }

        TextView ssid = (TextView)convertView.findViewById(R.id.txtSSID);
        TextView bssid = (TextView)convertView.findViewById(R.id.txtBSSID);
        TextView level = (TextView)convertView.findViewById(R.id.txtLevel);
        TextView count = (TextView)convertView.findViewById(R.id.txtSampleCount);
        TextView freq = (TextView)convertView.findViewById(R.id.txt_frequency);

        if (item != null) {
            ssid.setText(item.ap.ssid);
            bssid.setText(item.ap.mac);
            level.setText(String.format(Locale.JAPAN, "%.2f", item.pattern.averageLevel));
            count.setText(String.valueOf(item.pattern.sampleCount));
            freq.setText(String.format(Locale.JAPAN, "%.1f", ((float)item.ap.frequency)/1000));
        }

        return convertView;
    }
}
