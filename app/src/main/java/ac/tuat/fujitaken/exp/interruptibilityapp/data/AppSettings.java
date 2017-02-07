package ac.tuat.fujitaken.exp.interruptibilityapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment;

/**
 *
 * Created by hi on 2017/02/07.
 */

public class AppSettings {
    public final boolean ACC,
        NOTE;
    public final int PORT, ID, VOLUME;
    public final String IP_ADDRESS;

    public AppSettings(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        ACC = preferences.getBoolean(SettingFragment.ACC_SAVE, false);
        NOTE = preferences.getBoolean(SettingFragment.NOTE, true);
        IP_ADDRESS = preferences.getString(SettingFragment.IP_ADDRESS, "");
        PORT = preferences.getInt(SettingFragment.PORT, 54613);
        ID = preferences.getInt(SettingFragment.SP_ID, 10);
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        VOLUME = preferences.getInt(SettingFragment.VOLUME, manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
    }
}
