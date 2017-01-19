package ac.tuat.fujitaken.kk.test.testapplication.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Map;

import ac.tuat.fujitaken.kk.test.testapplication.R;

/**
 * イベント数を表示するためのアダプター
 * Created by Komuro on 15/07/03.
 */
public class EventAdapter extends ArrayAdapter{

    private LayoutInflater layoutInflater;

    public EventAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map.Entry item = (Map.Entry)getItem(position);

        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.item_layout, null);
        }

        TextView text1 = (TextView)convertView.findViewById(R.id.item);

        text1.setText(String.valueOf(item.getKey() + ", " +item.getValue()));

        return convertView;
    }
}
