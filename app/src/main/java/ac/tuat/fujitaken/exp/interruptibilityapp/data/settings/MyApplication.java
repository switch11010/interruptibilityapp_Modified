package ac.tuat.fujitaken.exp.interruptibilityapp.data.settings;

import android.app.Application;

/**
 *
 * Created by hi on 2017/02/28.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Settings.createInstance(this);
    }


}
