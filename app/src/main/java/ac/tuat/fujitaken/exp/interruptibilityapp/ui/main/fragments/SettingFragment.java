package ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.AppSettings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.service.MainService;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.MainActivity;

import static ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings.getAppSettings;

/**
 * 記録サービスの設定用フラグメント
 */
public class SettingFragment extends Fragment {

    //データを受け取るための定数
    private static final String ARG_POSITION = "param1";

    private SwitchCompat saveSwitch, noteSwitch;
    private TextView exist;
    private EditText ipText, spText, portText;
    private SeekBar volume;
    private ToggleButton togglePC, toggleSD;
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
        b.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            });

        manager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        volume = (SeekBar)root.findViewById(R.id.volume);
        volume.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        ipText = (EditText)root.findViewById(R.id.ip_address);
        spText = (EditText)root.findViewById(R.id.sp_id);
        portText = (EditText)root.findViewById(R.id.port_num);
        togglePC = (ToggleButton)root.findViewById(R.id.togglePC);
        toggleSD = (ToggleButton)root.findViewById(R.id.toggleSD);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        AppSettings settings = getAppSettings();

        saveSwitch.setChecked(settings.isAccSave());
        noteSwitch.setChecked(settings.isNoteMode());
        ipText.setText(settings.getIpAddress());
        spText.setText(String.valueOf(settings.getId()));
        portText.setText(String.valueOf(settings.getPort()));
        //togglePC.setChecked(settings.isPcMode());  //s コメントアウト
        togglePC.setChecked(false);  //s 変更：常時 OFF に（通知が来ない原因になる）
        toggleSD.setChecked(settings.isSaveMode());

        volume.setProgress(settings.getVolume());

        if(isServiceActive(Settings.getContext())) {  //s サービスが稼働中の場合
            switching(false);
        }
        else{
            switching(true);
        }
    }

    //s 設定画面のボタンを全て 有効化 / 無効化 する
    private void switching(boolean state){
        saveSwitch.setEnabled(state);
        noteSwitch.setEnabled(state);
        ipText.setEnabled(state);
        spText.setEnabled(state);
        portText.setEnabled(state);
        volume.setEnabled(state);
        //togglePC.setEnabled(state);  //s コメントアウト
        togglePC.setEnabled(false);  //s 変更：常時 Unable に（通知が来ない原因になる）
        toggleSD.setEnabled(state);
        exist.setVisibility(state? View.INVISIBLE: View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        AppSettings settings = Settings.getAppSettings();
        settings.setAccSave(saveSwitch.isChecked());
        settings.setNoteMode(noteSwitch.isChecked());
        settings.setPcMode(togglePC.isChecked());
        settings.setSaveMode(toggleSD.isChecked());
        settings.setIpAddress(ipText.getText().toString());
        settings.setId(Integer.parseInt(spText.getText().toString()));
        settings.setPort(Integer.parseInt(portText.getText().toString()));
        settings.setVolume(volume.getProgress());
        settings.refresh();
    }

    /**
     * 記録用サービスが生存しているかを確認する
     * @param context　アプリケーションのコンテキスト
     * @return サービスが生存していたらtrue
     */
    //s MainActivity.onSectionAttached(), ItemFragment.onListItemClick(), .onOptionsItemSelected(), this.onResume() で呼ばれる
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
