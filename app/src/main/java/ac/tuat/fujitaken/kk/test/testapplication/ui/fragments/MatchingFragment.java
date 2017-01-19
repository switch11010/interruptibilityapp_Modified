package ac.tuat.fujitaken.kk.test.testapplication.ui.fragments;


import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ac.tuat.fujitaken.kk.test.testapplication.R;
import ac.tuat.fujitaken.kk.test.testapplication.ui.activities.MainActivity;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.SpotSearch;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.WifiWatching;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data.Spot;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data.SpotAndDist;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.database.WiPSDBHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MatchingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MatchingFragment extends Fragment implements WifiWatching.ScanEventAction{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private TextView result;
    private Button matching, export;
    private SpotSearch search;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MatchingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MatchingFragment newInstance(int param1) {
        MatchingFragment fragment = new MatchingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public MatchingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.search = new SpotSearch(getActivity().getApplicationContext(), ((MainActivity)getActivity()).getDbHelper());
        search.setAction(this);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_PARAM1));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RelativeLayout root = (RelativeLayout)inflater.inflate(R.layout.fragment_matching, container, false);
        result = (TextView)root.findViewById(R.id.matching);
        matching = (Button)root.findViewById(R.id.matching_start);
        matching.setText("START");
        matching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!search.watching.manager.isWifiEnabled()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Wi-Fi");
                    builder.setMessage("Wi-Fiをオンにします");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            search.watching.manager.setWifiEnabled(true);
                            search.start();
                            matching.setText("STOP");
                        }
                    });
                    builder.setCancelable(true);
                    builder.show();
                } else {
                    if (search.searching) {
                        search.stop();
                        matching.setText("START");
                    } else {
                        search.start();
                        matching.setText("STOP");
                    }
                }
            }
        });
        export = (Button)root.findViewById(R.id.export);
        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (search.watching.manager.isWifiEnabled()) {
                    WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                    SQLiteDatabase db = helper.getReadableDatabase();
                    dumpToCsv();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        search.stop();
    }

    List<List<ScanResult>> buf = new ArrayList<>();

    @Override
    public void onScanResultAvailable() {
        StringBuilder builder = new StringBuilder();
        WiPSDBHelper helper = ((MainActivity)getActivity()).getDbHelper();
        SQLiteDatabase db = helper.getReadableDatabase();
        buf.add(search.watching.manager.getScanResults());
        if(buf.size() > 100){
            buf.remove(0);
        }
        for(SpotAndDist s:search.matching()){
            Spot spot = helper.selectSpot(db, s.getSpotId());
            builder.append(spot.name)
                    .append(" : ")
                    .append(String.format("%.5f\n", s.getDist()));
        }
        builder.append("----------\n");
        for(SpotAndDist s:search.matchingtest()){
            Spot spot = helper.selectSpot(db, s.getSpotId());
            builder.append(spot.name)
                    .append(" : ")
                    .append(String.format("%.5f\n", s.getDist()));
        }
        result.setText(builder.toString());
    }

    private void dumpToCsv(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/WIPS/" + sdf.format(System.currentTimeMillis()) + ".csv");
        file.getParentFile().mkdirs();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "SHIFT-JIS");
            BufferedWriter bw = new BufferedWriter(osw);
            List<StringBuilder> builders = new ArrayList<>();
            for(int i = 0; i < buf.size()+1; i++) {
                builders.add(new StringBuilder());
            }

            for(int i = 0; i < buf.size(); i++){
                List<ScanResult> parent = buf.get(i);
                Collections.sort(parent, new Comparator<ScanResult>() {
                    @Override
                    public int compare(ScanResult lhs, ScanResult rhs) {
                        return rhs.level - lhs.level;
                    }
                });
                while (parent.size() > 0) {
                    ScanResult column = parent.remove(0);

                    builders.get(0).append(column.BSSID.replaceAll(":", ""))
                            .append(":")
                            .append(String.format("%.1f", (float)column.frequency/1000))
                            .append(",");
                    builders.get(i + 1).append(column.level).append(",");

                    for (int n = i + 1; n < buf.size(); n++) {
                        List<ScanResult> row = buf.get(n);
                        for (int m = 0; m < row.size(); m++) {
                            ScanResult w = row.get(m);
                            if (w.BSSID.equals(column.BSSID)) {
                                builders.get(n+1).append(w.level);
                                row.remove(m);
                                break;
                            }
                        }
                        builders.get(n+1).append(",");
                    }
                }
            }
            for(StringBuilder builder: builders) {
                bw.write(builder.toString());
                bw.newLine();
            }

            bw.close();
            MediaScannerConnection.scanFile(getActivity().getApplicationContext(), new String[]{file.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
