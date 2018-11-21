package ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.Calendar;

import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;  //s 自作Log
import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.DeviceSettings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;
import ac.tuat.fujitaken.exp.interruptibilityapp.interruption.NotificationController;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.MainActivity;

/**
 * s 追加：デバッグ情報表示用フラグメント（SettingFragmentから大部分を削ぎ落とし）
 */
public class DebugInfoFragment extends Fragment {

    //データを受け取るための定数
    private static final String ARG_POSITION = "param1";

    private TextView text;  //s デバッグ情報が表示される文字列の部分


    public static Fragment newInstance(int position){
        DebugInfoFragment fragment = new DebugInfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public DebugInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_POSITION));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_debug_info, null);

        text = (TextView)root.findViewById(R.id.textView);
        text.setOnClickListener( v -> text.setText(makeDebugMessage()) );

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        text.setText(makeDebugMessage());
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    //s デバッグ情報を取得＆文字列にして返す
    private String makeDebugMessage() {
        final String t = "        ";
        final String n = "\n";
        final String hr = "--------".replace("-", "--------") + n;

        String str = "";

        //s 時刻表示
        str += "取得日時: " + Calendar.getInstance().getTime() + n;
        str += hr;
        str += n;

        //s 各種権限の確認
        DeviceSettings deviceSettings = Settings.getDeviceSettings();

        str += "isUsagePermissionGranted: "    + deviceSettings.isUsagePermissionGranted     + n;
        str += "isStoragePermissionGranted: "  + deviceSettings.isStoragePermissionGranted   + n;
        str += "isPhonePermissionGranted: "    + deviceSettings.isPhonePermissionGranted     + n;
        str += "isLocationPermissionGranted: " + deviceSettings.isLocationPermissionGranted  + n;
        str += n;

        str += hr;
        str += n;

        //s SDカードの書き込み場所の確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            str += "SaveData.getSdCardFilesDirPathListForLollipop(): ";
            String path = SaveData.getSdCardFilesDirPathListForLollipop(Settings.getContext());
            if (path.length() == 0) path = "SDカードなし";
            str += path + n + n;
        }

        //s SaveData のフィールドやらの確認
        SaveData[] saveDataArr = {
                (NotificationController.getInstance() != null ? NotificationController.getInstance().getEvaluationData() : null),
                LogEx.getSaveData(),
        };
        String[] ClassNames = {
                "NotificationController",
                "LogEx",
        };
        for (int i = 0; i < saveDataArr.length; i++) {
            SaveData saveData = saveDataArr[i];
            String className = ClassNames[i];

            str += className + ".saveData: " + saveData + n;
            if (saveData != null) {
                str += t + className + ".saveData.lock: " + saveData.lock + n;
                str += t + className + ".saveData.getFile(): " + saveData.getFile() + n;
                str += t + className + ".saveData.getHeader(): " + saveData.getHeader() + n;
            }
            str += n;
        }

        str += hr;
        str += n;

        //s サービスが稼働中かどうか
        Context context = Settings.getContext();
        str += "isServiceActive: " + SettingFragment.isServiceActive(context) + n;
        str += n;

        str += hr;
        str += n;

        //s ファイル書き込み権限やらなにやら
        str += "＠適当ファイル書き込み権限確認コーナー＠\n";
        str += "SDK_INT: " + Build.VERSION.SDK_INT + n;
        str += "M: " + Build.VERSION_CODES.M + n;
        str += "\tSDK_INT > M: " + (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) + n;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            str += "context.checkSelfPermission(WRITE_EXTERNAL_STORAGE): " + context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) + n;
            str += "PackageManager.PERMISSION_GRANTED: " + PackageManager.PERMISSION_GRANTED + n;
            str += "上2つの比較: " + (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) + n;
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                str += "ファイル書き込み権限：false" + n;
            } else {
                str += "ファイル書き込み権限：true" + n;
            }
        } else {
            str += "ファイル書き込み権限：true ( SDK_INT < M )" + n;
        }
        str += n;

        str += hr;
        str += n;



        return str;
    }
}
