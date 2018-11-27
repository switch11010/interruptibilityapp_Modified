package ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;
import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.AppSettings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.MainActivity;

/**
 * 記録サービスの設定用フラグメント（SettingFragmentのコピペ）
 */
public class SettingFragment_Ex extends Fragment {

    //データを受け取るための定数
    private static final String ARG_POSITION = "param1";

    /*private SwitchCompat saveSwitch, noteSwitch;*/
    private SwitchCompat forceNoteSwitch;
    private SwitchCompat noNoteOnWalkSwitch;
    private EditText lockScreenOffSecText;
    private SwitchCompat noteOnAppChangeSwitch;
    /*private TextView exist;
    private EditText ipText, spText, portText;
    private SeekBar volume;
    private ToggleButton togglePC, toggleSD;
    private AudioManager manager;*/
    private SwitchCompat noUseSwitch;
    private TextView noUseSwitchText;


    public static Fragment newInstance(int position){
        SettingFragment_Ex fragment = new SettingFragment_Ex();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingFragment_Ex() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_POSITION));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_setting_ex, null);
        /*saveSwitch = (SwitchCompat)root.findViewById(R.id.saveSwitch);
        noteSwitch = (SwitchCompat)root.findViewById(R.id.noteSwitch);*/
        forceNoteSwitch = (SwitchCompat)root.findViewById(R.id.forceNoteSwitch);  //s 通知の強制
        noNoteOnWalkSwitch = (SwitchCompat)root.findViewById(R.id.noNoteOnWalkSwitch);  //s 歩行時通知配信抑制
        lockScreenOffSecText = (EditText)root.findViewById(R.id.lockScreenOffSecText);  //s ロック画面自動消灯時間

        Button b1 = (Button)root.findViewById(R.id.moveBatterySaverSettingsButton);  //s 電池最適化の設定画面表示ボタン
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            b1.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
            });
        } else {
            b1.setEnabled(false);  //s 非対応なので押せないようにしとく
        }

        Button b2 = (Button)root.findViewById(R.id.moveNotificationPolicySettingsButton);  //s 通知非表示の設定画面表示ボタン
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            b2.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            });
        } else {
            b2.setEnabled(false);  //s 非対応なので押せないようにしとく
        }

        noteOnAppChangeSwitch = (SwitchCompat)root.findViewById(R.id.noteOnAppChangeSwitch);  //s アプリ切替で通知

        noUseSwitch = (SwitchCompat)root.findViewById(R.id.noUseSwitch);  //s 押してはいけないスイッチ
        noUseSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            onNoUseSwitchClicked(isChecked);
        });
        noUseSwitchText = (TextView)root.findViewById(R.id.noUseSwitchText);

        /*exist = (TextView)root.findViewById(R.id.isExistService);
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
        toggleSD = (ToggleButton)root.findViewById(R.id.toggleSD);*/

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        AppSettings settings = Settings.getAppSettings();

        /*saveSwitch.setChecked(settings.isAccSave());
        noteSwitch.setChecked(settings.isNoteMode());*/
        forceNoteSwitch.setChecked(settings.isForceNoteMode());
        noNoteOnWalkSwitch.setChecked(settings.isNoNoteOnWalkMode());
        lockScreenOffSecText.setText(String.valueOf(settings.getLockScreenOffSec()));
        noteOnAppChangeSwitch.setChecked(settings.isNoteOnAppChangeMode());
        /*ipText.setText(settings.getIpAddress());
        spText.setText(String.valueOf(settings.getId()));
        portText.setText(String.valueOf(settings.getPort()));
        togglePC.setChecked(settings.isPcMode());
        toggleSD.setChecked(settings.isSaveMode());*/

        /*volume.setProgress(settings.getVolume());

        if(isServiceActive(Settings.getContext())) {
            switching(false);
        }
        else{
            switching(true);
        }*/
    }

    private void switching(boolean state){
        /*saveSwitch.setEnabled(state);
        noteSwitch.setEnabled(state);*/
        forceNoteSwitch.setEnabled(state);
        noNoteOnWalkSwitch.setEnabled(state);
        lockScreenOffSecText.setEnabled(state);
        noteOnAppChangeSwitch.setEnabled(state);
        /*ipText.setEnabled(state);
        spText.setEnabled(state);
        portText.setEnabled(state);
        volume.setEnabled(state);
        togglePC.setEnabled(state);
        toggleSD.setEnabled(state);
        exist.setVisibility(state? View.INVISIBLE: View.VISIBLE);*/
    }

    @Override
    public void onPause() {
        super.onPause();

        AppSettings settings = Settings.getAppSettings();
        /*settings.setAccSave(saveSwitch.isChecked());
        settings.setNoteMode(noteSwitch.isChecked());*/
        settings.setForceNoteMode(forceNoteSwitch.isChecked());
        settings.setNoNoteOnWalkMode(noNoteOnWalkSwitch.isChecked());
        settings.setLockScreenOffSec(Integer.parseInt(lockScreenOffSecText.getText().toString()));
        settings.setNoteOnAppChangeMode(noteOnAppChangeSwitch.isChecked());
        /*settings.setPcMode(togglePC.isChecked());
        settings.setSaveMode(toggleSD.isChecked());
        settings.setIpAddress(ipText.getText().toString());
        settings.setId(Integer.parseInt(spText.getText().toString()));
        settings.setPort(Integer.parseInt(portText.getText().toString()));
        settings.setVolume(volume.getProgress());*/
        settings.refresh();
    }

    /*/**
     * 記録用サービスが生存しているかを確認する
     * @param context　アプリケーションのコンテキスト
     * @return サービスが生存していたらtrue
     */
    /*public static boolean isServiceActive(Context context){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> listServiceInfo = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo curr : listServiceInfo) {
            if (curr.service.getClassName().equals(MainService.class.getName())) {
                return true;
            }
        }
        return false;
    }*/


    //s ぶるぶるするやつ
    private void onNoUseSwitchClicked(boolean isChecked) {
        long pattern1 = 10 * 60 * 1000;  //s 10m
        long[] pattern2 = {
                0,
                100, 100, 100, 100,
                100, 100, 50, 50,
                100, 100, 100, 100, 50, 50,
                100, 100, 100, 100,
        };
        final int pattern = 1;

        Vibrator v = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (isChecked) {
            noUseSwitchText.setText("うわあああああああ");
            if (pattern == 1) {
                v.vibrate(pattern1);
            } else if (pattern == 2) {
                v.vibrate(pattern2, 0);
            }
            noUseSwitchText.setTextColor(0xFFFF0000);
        } else {
            noUseSwitchText.setTextColor(0x8A000000);
            noUseSwitchText.setText("死ぬかと思った");
            v.cancel();
        }
    }
}
