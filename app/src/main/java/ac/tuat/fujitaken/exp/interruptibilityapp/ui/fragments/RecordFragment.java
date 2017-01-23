package ac.tuat.fujitaken.exp.interruptibilityapp.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.activities.MainActivity;
import ac.tuat.fujitaken.exp.interruptibilityapp.receiver.wifi.ApRecording;
import ac.tuat.fujitaken.exp.interruptibilityapp.receiver.wifi.WifiWatching;
import ac.tuat.fujitaken.exp.interruptibilityapp.receiver.wifi.data.ListItem;
import ac.tuat.fujitaken.exp.interruptibilityapp.receiver.wifi.data.Spot;
import ac.tuat.fujitaken.exp.interruptibilityapp.receiver.wifi.data.WifiAdapter;
import ac.tuat.fujitaken.exp.interruptibilityapp.receiver.wifi.database.WiPSDBHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment implements WifiWatching.ScanEventAction{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SPOT_ID = "param1";

    // TODO: Rename and change types of parameters
    private Button scan;
    private EditText spotName;
    private ApRecording apRecording;
    private WifiAdapter adapter;
    private WifiManager wifiManager;
    private boolean isReceiverRegistered = false;
    private Spot spot;

    //private WifiWatching watching;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordFragment newInstance(int spotID) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SPOT_ID, spotID);
        fragment.setArguments(args);
        return fragment;
    }

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        if(getArguments() != null) {
            spot = new Spot(getArguments().getInt(ARG_SPOT_ID), "", -1, -1);
            if(spot.id >= 0){
                readDB();
            }
            else{
                apRecording = new ApRecording(getActivity().getApplicationContext());
            }
            apRecording.setAction(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RelativeLayout root = (RelativeLayout)inflater.inflate(R.layout.fragment_record, container, false);
        scan = (Button)root.findViewById(R.id.scan);
        spotName = (EditText)root.findViewById(R.id.spot_name);
        spotName.setText(spot.name);
        ListView apList = (ListView)root.findViewById(R.id.record_list);
        adapter = new WifiAdapter(getActivity().getApplicationContext(), R.layout.list_wifiap_item);
        apList.setAdapter(adapter);
        adapter.addAll(apRecording.toList());

        scan.setText("SCAN");
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReceiverRegistered) {
                    stop();
                    scan.setText("SCAN");
                } else if (!wifiManager.isWifiEnabled()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Wi-Fi");
                    builder.setMessage("Wi-Fiをオンにします");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            wifiManager.setWifiEnabled(true);
                            start();
                            scan.setText("STOP");
                        }
                    });
                    builder.setCancelable(true);
                    builder.show();
                } else {
                    start();
                    scan.setText("STOP");
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isReceiverRegistered) {
            stop();
        }
        writeDB();
    }

    private void start(){
        apRecording.start();
        isReceiverRegistered = true;
    }

    private void stop(){
        apRecording.stop();
        isReceiverRegistered = false;
    }

    private void writeDB(){
        WiPSDBHelper helper = ((MainActivity)getActivity()).getDbHelper();
        SQLiteDatabase db = helper.getWritableDatabase();

        spot.name = spotName.getText().toString();

        helper.insertSpot(db, spot);

        helper.insertAPPatternMap(db, spot, apRecording.getRecordMap());

        db.close();
    }

    private void readDB(){
        WiPSDBHelper helper = ((MainActivity)getActivity()).getDbHelper();
        SQLiteDatabase db = helper.getReadableDatabase();

        Spot spot = helper.selectSpot(db, this.spot.id);
        this.spot = spot;
        apRecording = new ApRecording(getActivity().getApplicationContext(), helper.selectPatternFromSpotID(db, spot.id));
        db.close();
    }

    @Override
    public void onScanResultAvailable() {
        List<ListItem> list = apRecording.toList();
        adapter.clear();
        adapter.addAll(list);
    }
}
