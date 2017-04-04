package ac.tuat.fujitaken.exp.interruptibilityapp.data.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;

import static ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment.ACC_SAVE;
import static ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment.IP_ADDRESS;
import static ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment.NOTE;
import static ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment.PORT;
import static ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment.SP_ID;
import static ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment.VOLUME;

/**
 *
 * Created by hi on 2017/02/07.
 */

@SuppressWarnings("UnusedDeclaration")
public class AppSettings extends Application{
    private boolean accSave, noteMode;
    private int port, id, volume;
    private String ipAddress;
    private SharedPreferences preferences;

    AppSettings(){
        Context context = Settings.getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        accSave = preferences.getBoolean(ACC_SAVE, false);
        noteMode = preferences.getBoolean(NOTE, true);
        ipAddress = preferences.getString(IP_ADDRESS, "");
        port = preferences.getInt(PORT, Constants.DEFAULT_PORT);
        id = preferences.getInt(SP_ID, Constants.DEFAULT_SP_ID);
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        volume = preferences.getInt(VOLUME, manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
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

    void refresh(){
        if(preferences == null){
            preferences = PreferenceManager.getDefaultSharedPreferences(Settings.getContext());
        }
        //設定の保存
        preferences.edit()
                .putBoolean(ACC_SAVE, accSave)
                .putBoolean(NOTE, noteMode)
                .putString(IP_ADDRESS, ipAddress)
                .putInt(SP_ID, id)
                .putInt(PORT, port)
                .putInt(VOLUME, volume)
                .apply();
    }
}
