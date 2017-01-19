package ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.database;

import android.os.Environment;
import android.provider.BaseColumns;

/**
 * Created by igna on 10/31/2014.
 * WiPS (Wi-Fi Positioning System) Database Contracts
 * データベース名, テーブル名、列名の定数を定義するクラス。
 * Version History :
 *   1. 初期バージョン
 */
public final class WiPSDBContract {

    //このクラスには定数しか入っていないので、
    //作成できないようにする。
    private WiPSDBContract() {}

    //データベース情報
    public static final int DATABASE_VERSION = 1;
    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/WIPS/",
            DATABASE_NAME = "wips.db";

    //TABLE AccessPoints : 学習したアクセスポイントの一覧
    public static abstract class AccessPoints implements BaseColumns {
        public static final String TABLE_NAME = "AccessPoints";

        //アクセスポイントのID番号 (INTEGER AUTOINCREMENT)
        public static final String ID = "id";
        //アクセスポイントのMACアドレス (TEXT)
        public static final String MAC_ADDRESS = "mac";
        //アクセスポイントのSSID名（ネットワーク名)  (TEXT)
        public static final String SSID = "ssid";
        //アクセスポイントの周波数 (INTEGER)
        public static final String FREQUENCY = "frequency";

        /** テーブル操作のSQL文 **/
        //テーブル作成
        public static final String OPERATION_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MAC_ADDRESS + " TEXT UNIQUE, " +
                        SSID + " TEXT, " +
                        FREQUENCY + " INTEGER" +
                ")";

        //テーブル削除
        public static final String OPERATION_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Spots implements BaseColumns {
        public static final String TABLE_NAME = "Spots";

        //場所のID (INTEGER AUTOINCREMENT)
        public static final String ID = "id";
        //場所の名称 (INTEGER)
        public static final String NAME = "name";
        //場所の緯度 (REAL)
        public static final String LATITUDE = "latitude";
        //場所の経度 (REAL)
        public static final String LONGITUDE = "longitude";

        /** テーブル操作のSQL文 **/
        //テーブル作成
        public static final String OPERATION_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        NAME + " TEXT, " +
                        LATITUDE + " REAL, " +
                        LONGITUDE + " REAL " +
                        ")";

        //テーブル削除
        public static final String OPERATION_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Patterns implements BaseColumns {
        public static final String TABLE_NAME = "Patterns";

        //このパターンが対応する場所のID  (INTEGER)
        public static final String SPOT_ID = "spotid";
        //このパターンが対応するアクセスポイントのID (INTEGER)
        public static final String ACCESS_POINT_ID = "apid";
        //このパターンのアクセスポイントの平均電波レベル [dBm] (REAL)
        public static final String AVERAGE_LEVEL = "avglevel";
        //このパターンのアクセスポイント平均電波レベルのサンプル数 (INTEGER)
        public static final String SAMPLE_COUNT = "samplecnt";

        /** テーブル操作のSQL文 **/
        //テーブル作成
        public static final String OPERATION_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        SPOT_ID + " INTEGER REFERENCES " + Spots.TABLE_NAME + "(" + Spots.ID + ")"
                            + " ON DELETE CASCADE, " +
                        ACCESS_POINT_ID + " INTEGER REFERENCES " + AccessPoints.TABLE_NAME + "(" + AccessPoints.ID + ")"
                            + " ON DELETE CASCADE, " +
                        AVERAGE_LEVEL + " REAL, " +
                        SAMPLE_COUNT + " INTEGER, " +
                        "UNIQUE (" + SPOT_ID + "," + ACCESS_POINT_ID + ")" +
                        ")";

        //テーブル削除
        public static final String OPERATION_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }



}
