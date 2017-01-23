package ac.tuat.fujitaken.exp.interruptibilityapp.ui.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.activities.MainActivity;
import ac.tuat.fujitaken.exp.interruptibilityapp.service.MainService;

/**
 * 記録サービスの設定用フラグメント
 */
public class SettingFragment extends Fragment {

    //データを受け取るための定数
    private static final String ARG_POSITION = "param1";

    //設定保存のための定数
    public static final String ACC_SAVE = "acc_save",
            NOTE = "note",
            VOLUME = "volume",
            IP_ADDRESS = "ip_address",
            SP_ID = "sp_id",
            PORT = "port_num";

    private SwitchCompat saveSwitch, noteSwitch;
    private TextView exist;
    private EditText ipText, spText, portText;
    private SharedPreferences preferences;
    private SeekBar volume;
    private AudioManager manager;

    public static Fragment newInstance(int position){
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_POSITION));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_setting, null);
        saveSwitch = (SwitchCompat)root.findViewById(R.id.saveSwitch);
        noteSwitch = (SwitchCompat)root.findViewById(R.id.noteSwitch);

        exist = (TextView)root.findViewById(R.id.isExistService);
        exist.setVisibility(View.INVISIBLE);

        Button b = (Button)root.findViewById(R.id.moveSettings);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });
        manager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        volume = (SeekBar)root.findViewById(R.id.volume);
        volume.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        ipText = (EditText)root.findViewById(R.id.ip_address);
        spText = (EditText)root.findViewById(R.id.sp_id);
        portText = (EditText)root.findViewById(R.id.port_num);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(preferences == null){
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        }

        saveSwitch.setChecked(preferences.getBoolean(ACC_SAVE, true));
        noteSwitch.setChecked(preferences.getBoolean(NOTE, true));
        ipText.setText(preferences.getString(IP_ADDRESS, ""));
        spText.setText(String.valueOf(preferences.getInt(SP_ID, 10)));
        portText.setText(String.valueOf(preferences.getInt(PORT, 54613)));

        volume.setProgress(preferences.getInt(VOLUME, manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)));

        if(isServiceActive(getActivity().getApplicationContext())) {
            switching(false);
        }
        else{
            switching(true);
        }
    }

    private void switching(boolean state){
        saveSwitch.setEnabled(state);
        noteSwitch.setEnabled(state);
        ipText.setEnabled(state);
        spText.setEnabled(state);
        portText.setEnabled(state);
        volume.setEnabled(state);
        exist.setVisibility(state? View.INVISIBLE: View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(preferences == null){
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        }
        //設定の保存
        preferences.edit()
                .putBoolean(ACC_SAVE, saveSwitch.isChecked())
                .putBoolean(NOTE, noteSwitch.isChecked())
                .putString(IP_ADDRESS, ipText.getText().toString())
                .putInt(SP_ID, Integer.parseInt(spText.getText().toString()))
                .putInt(PORT, Integer.parseInt(portText.getText().toString()))
                .putInt(VOLUME, volume.getProgress())
                .apply();
    }

    /**
     * 記録用サービスが生存しているかを確認する
     * @param context　アプリケーションのコンテキスト
     * @return サービスが生存していたらtrue
     */
    public static boolean isServiceActive(Context context){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> listServiceInfo = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo curr : listServiceInfo) {
            if (curr.service.getClassName().equals(MainService.class.getName())) {
                return true;
            }
        }
        return false;
    }
}
