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

public class AppSettings extends Application{  //s extends Application を抜いてもいい気がする
    //設定保存のための定数
    public static final String ACC_SAVE = "acc_save",
            NOTE = "note",
            VOLUME = "volume",
            IP_ADDRESS = "ip_address",
            SP_ID = "sp_id",
            PORT = "port_num",
            PC_MODE = "pc_mode",
            SAVE_MODE = "save_mode",
            FORCE_NOTE = "force_note",  //s 追加ここから
            NO_NOTE_ON_WALK = "no_note_on_walk",
            LOCK_SCREEN_OFF_SEC = "lock_screen_off_sec",
            NOTE_ON_APP_CHANGE = "note_on_app_change";  //s 追加ここまで

    private boolean accSave, noteMode, pcMode, saveMode;
    private int port, id, volume;
    private String ipAddress;
    private SharedPreferences preferences;
    private boolean forceNoteMode;  //s 追加ここから
    private boolean noNoteOnWalkMode;
    private int lockScreenOffSec;
    private boolean noteOnAppChangeMode;  //s 追加ここまで

    AppSettings(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        accSave = preferences.getBoolean(ACC_SAVE, false);
        noteMode = preferences.getBoolean(NOTE, true);
        pcMode = preferences.getBoolean(PC_MODE, false);
        saveMode = preferences.getBoolean(SAVE_MODE, false);
        ipAddress = preferences.getString(IP_ADDRESS, "");
        port = preferences.getInt(PORT, Constants.DEFAULT_PORT);
        id = preferences.getInt(SP_ID, Constants.DEFAULT_SP_ID);
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            volume = preferences.getInt(VOLUME, manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        }
        forceNoteMode = preferences.getBoolean(FORCE_NOTE, false);  //s 追加ここから
        noNoteOnWalkMode = preferences.getBoolean(NO_NOTE_ON_WALK, false);
        lockScreenOffSec = preferences.getInt(LOCK_SCREEN_OFF_SEC, 10);
        noteOnAppChangeMode = preferences.getBoolean(NOTE_ON_APP_CHANGE, false);  //s 追加ここまで
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
    }  //s 追加ここから
    public void setNoNoteOnWalkMode(boolean b) {
        this.noNoteOnWalkMode = b;
    }
    public void setLockScreenOffSec(int i) {
        this.lockScreenOffSec = i;
    }
    public void setNoteOnAppChangeMode(boolean b) {
        this.noteOnAppChangeMode = b;
    }  //s 追加ここまで

    public boolean isAccSave() {
        return accSave;
    }
    public int      getId() {
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
    }  //s 追加ここから
    public boolean isNoNoteOnWalkMode() {
        return noNoteOnWalkMode;
    }
    public int      getLockScreenOffSec() {
        return lockScreenOffSec;
    }
    public boolean isNoteOnAppChangeMode() {
        return noteOnAppChangeMode;
    }  //s 追加ここまで

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
        editor.putBoolean(FORCE_NOTE, forceNoteMode);  //s 追加ここから
        editor.putBoolean(NO_NOTE_ON_WALK, noNoteOnWalkMode);
        editor.putInt(LOCK_SCREEN_OFF_SEC, lockScreenOffSec);
        editor.putBoolean(NOTE_ON_APP_CHANGE, noteOnAppChangeMode);  //s 追加ここまで
        editor.apply();
    }
}
