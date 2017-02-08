package ac.tuat.fujitaken.exp.interruptibilityapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment;

/**
 *
 * Created by hi on 2017/02/07.
 */

public class AppSettings {
    private boolean accSave, noteMode;
    private int port, id, volume;
    private String ipAddress;

    public AppSettings(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        accSave = preferences.getBoolean(SettingFragment.ACC_SAVE, false);
        noteMode = preferences.getBoolean(SettingFragment.NOTE, true);
        ipAddress = preferences.getString(SettingFragment.IP_ADDRESS, "");
        port = preferences.getInt(SettingFragment.PORT, Constants.DEFAULT_PORT);
        id = preferences.getInt(SettingFragment.SP_ID, Constants.DEFAULT_SP_ID);
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        volume = preferences.getInt(SettingFragment.VOLUME, manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
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

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public int getVolume() {
        return volume;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isAccSave() {
        return accSave;
    }

    public boolean isNoteMode() {
        return noteMode;
    }
}
