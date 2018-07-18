package ac.tuat.fujitaken.exp.interruptibilityapp.data.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;

/**
 *
 * Created by hi on 2017/02/07.
 */

public class AppSettings extends Application{
    //設定保存のための定数
    public static final String ACC_SAVE = "acc_save",
            NOTE = "note",
            FORCE_NOTE = "force_note",  // 追加
            VOLUME = "volume",
            IP_ADDRESS = "ip_address",
            SP_ID = "sp_id",
            PORT = "port_num",
            PC_MODE = "pc_mode",
            SAVE_MODE = "save_mode";

    private boolean accSave, noteMode, pcMode, saveMode;
    private boolean forceNoteMode;  // 追加
    private int port, id, volume;
    private String ipAddress;
    private SharedPreferences preferences;

    AppSettings(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        accSave = preferences.getBoolean(ACC_SAVE, false);
        noteMode = preferences.getBoolean(NOTE, true);
        forceNoteMode = preferences.getBoolean(FORCE_NOTE, false);  // 追加
        pcMode = preferences.getBoolean(PC_MODE, false);
        saveMode = preferences.getBoolean(SAVE_MODE, false);
        ipAddress = preferences.getString(IP_ADDRESS, "");
        port = preferences.getInt(PORT, Constants.DEFAULT_PORT);
        id = preferences.getInt(SP_ID, Constants.DEFAULT_SP_ID);
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            volume = preferences.getInt(VOLUME, manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        }
    }

    public void setAccSave(boolean b) {
        this.accSave = b;
    }
    public void setId(int i) {
        this.id = i;
    }
    public void setIpAddress(String s) {
        this.ipAddress = s;
    }
    public void setNoteMode(boolean b) {
        this.noteMode = b;
    }
    public void setPort(int i) {
        this.port = i;
    }
    public void setVolume(int i) {
        this.volume = i;
    }
    public void setPcMode(boolean pcMode) {
        this.pcMode = pcMode;
    }
    public void setSaveMode(boolean saveMode) {
        this.saveMode = saveMode;
    }
    public void setForceNoteMode(boolean b) {
        this.forceNoteMode = b;
    }  // 追加

    public boolean isAccSave() {
        return accSave;
    }
    public int     getId() {
        return id;
    }
    public String   getIpAddress() {
        return ipAddress;
    }
    public boolean isNoteMode() {
        return noteMode;
    }
    public int      getPort() {
        return port;
    }
    public int      getVolume() {
        return volume;
    }
    public boolean isPcMode() {
        return pcMode;
    }
    public boolean isSaveMode() {
        return saveMode;
    }
    public boolean isForceNoteMode() {
        return forceNoteMode;
    }  // 追加

    public void refresh(){
        if(preferences == null){
            preferences = PreferenceManager.getDefaultSharedPreferences(Settings.getContext());
        }
        //設定の保存
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ACC_SAVE, accSave);
        editor.putBoolean(NOTE, noteMode);
        editor.putString(IP_ADDRESS, ipAddress);
        editor.putInt(SP_ID, id);
        editor.putInt(PORT, port);
        editor.putInt(VOLUME, volume);
        editor.putBoolean(PC_MODE, pcMode);
        editor.putBoolean(SAVE_MODE, saveMode);
        editor.putBoolean(FORCE_NOTE, forceNoteMode);  // 追加
        editor.apply();
    }
}
