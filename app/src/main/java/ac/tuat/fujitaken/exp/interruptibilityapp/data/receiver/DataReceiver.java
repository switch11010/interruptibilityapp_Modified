package ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver;

import java.util.Map;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;

/**
 * イベントを受け取るクラスを定義するインタフェース
 * Created by hi on 2015/11/12.
 */
public interface DataReceiver {
        String  ACCELEROMETER = "ACCELEROMETER",

                MAGNETIC_FIELD = "MAGNETIC_FIELD,,",
                GYROSCOPE = "GYROSCOPE,,",
                PROXIMITY = "PROXIMITY",
                LIGHT = "LIGHT",

                NOTIFICATION = "NOTIFICATION_ON",

                WINDOW_STATE_CHANGED = "WINDOW_STATE_CHANGED",
                WINDOW_CONTENT_CHANGED = "WINDOW_CONTENT_CHANGED",
                VIEW_FOCUSED = "VIEW_FOCUSED",
                VIEW_SELECTED = "VIEW_SELECTED",
                VIEW_CLICKED = "VIEW_CLICKED",
                VIEW_LONG_CLICKED = "VIEW_LONG_CLICKED",
                VIEW_SCROLLED = "VIEW_SCROLLED",
                VIEW_TEXT_CHANGED = "VIEW_TEXT_CHANGED",
                VIEW_TEXT_SELECTION_CHANGED = "VIEW_TEXT_SELECTION_CHANGED",
                FOCUS_INPUT = "FOCUS_INPUT",
                //TYPE_TOUCH_INTERACTION_START = "TYPE_TOUCH_INTERACTION_START",

                APPLICATION = "APPLICATION",

                TOTAL_MEMORY = "TOTAL_MEMORY",

                HEADSET_PLUG = "HEADSET_PLUG",
                POWER_CONNECTED = "POWER_CONNECTED",
                SCREEN_ON = "SCREEN_ON",
                UNLOCKED = "UNLOCK",  //s 追加：ロック解除済かどうか
                RINGER_MODE = "RINGER_MODE",
                PHONE = "PHONE",

                WALK = "WALK",
                WIFI = "WIFI";

        /**
         * データを書き込み順で格納した配列
         */
        String[] NAMES = {
                APPLICATION,

                NOTIFICATION,

                HEADSET_PLUG ,
                POWER_CONNECTED,
                SCREEN_ON,
                UNLOCKED,  //s 追加：ロック解除済かどうか
                RINGER_MODE,
                PHONE,

                WALK,

                WINDOW_STATE_CHANGED,
                WINDOW_CONTENT_CHANGED,
                VIEW_FOCUSED,
                VIEW_SELECTED,
                VIEW_CLICKED,
                VIEW_LONG_CLICKED,
                VIEW_SCROLLED,
                VIEW_TEXT_CHANGED,
                VIEW_TEXT_SELECTION_CHANGED,
                FOCUS_INPUT,
                TOTAL_MEMORY,
                //TYPE_TOUCH_INTERACTION_START,

                MAGNETIC_FIELD,
                GYROSCOPE,
                PROXIMITY ,
                LIGHT,

                WIFI
        };

        Map<String, Data> getData();
}
