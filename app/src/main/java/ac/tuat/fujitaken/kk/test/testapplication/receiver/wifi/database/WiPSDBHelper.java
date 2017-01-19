package ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data.AccessPoint;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data.Pattern;
import ac.tuat.fujitaken.kk.test.testapplication.receiver.wifi.data.Spot;

/**
 * 場所、WiFi アクセスポイントと各場所でのアクセスポイントの電波強度パターンデータベースの管理と操作
 * を行うクラス
 */
public class WiPSDBHelper extends SQLiteOpenHelper {

    private Context mContext;

    public WiPSDBHelper(Context context) {
        super(context, WiPSDBContract.DATABASE_NAME, null, WiPSDBContract.DATABASE_VERSION);
        //File file = new File(WiPSDBContract.FILE_PATH);
        //file.getParentFile().mkdir();
        mContext = context;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        //デフォルトでは無効になっているので、
        //ForeignKey制約を必ず最初に有効にすること
        //アクセスポイント、場所とパターンの整合性を保つのに必要
        //ただし対応はsqlite 3.6.19以降 (Android ICS 4.1.1 (API 16) 以降)
        if(!db.isReadOnly()) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WiPSDBContract.AccessPoints.OPERATION_CREATE_TABLE);
        db.execSQL(WiPSDBContract.Spots.OPERATION_CREATE_TABLE);
        db.execSQL(WiPSDBContract.Patterns.OPERATION_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //初期バージョンなので今のところアップグレード未対応
        throw new UnsupportedOperationException
                ("WiPSDBHelper : Only V1 is implemented right now." +
                        " Upgrading DB is not supported yet.");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //ダウングレード対応なし
        throw new UnsupportedOperationException
                ("WiPSDBHelper : Downgrading DB is unsupported.");
    }

    public void dropAllTables(SQLiteDatabase db) {
        db.execSQL(WiPSDBContract.AccessPoints.OPERATION_DROP_TABLE);
        db.execSQL(WiPSDBContract.Spots.OPERATION_DROP_TABLE);
        db.execSQL(WiPSDBContract.Patterns.OPERATION_DROP_TABLE);
    }

    public void insertAccessPoint(SQLiteDatabase db, AccessPoint ap) {
        ContentValues values = new ContentValues();
        values.put(WiPSDBContract.AccessPoints.MAC_ADDRESS, ap.mac);
        values.put(WiPSDBContract.AccessPoints.SSID, ap.ssid);
        values.put(WiPSDBContract.AccessPoints.FREQUENCY, ap.frequency);

        long ret = db.insertWithOnConflict(WiPSDBContract.AccessPoints.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    //TODO:未テスト  一気にたくさん挿入する場合はこれのほうが速い。
    public void insertAccessPoints(SQLiteDatabase db, AccessPoint[] aps) {
        SQLiteStatement insertStatement = db.compileStatement
                ("INSERT OR IGNORE INTO " + WiPSDBContract.AccessPoints.TABLE_NAME + " ( " +
                    WiPSDBContract.AccessPoints.MAC_ADDRESS + "," +
                    WiPSDBContract.AccessPoints.SSID + ", " +
                    WiPSDBContract.AccessPoints.FREQUENCY + ") VALUES (?,?,?)");

        db.beginTransaction();
        try {
            boolean hasFail = false;
            for (AccessPoint ap : aps) {
                insertStatement.clearBindings();
                insertStatement.bindString(1, ap.mac);
                insertStatement.bindString(2, ap.ssid);
                insertStatement.bindLong(3, ap.frequency);
                hasFail = insertStatement.executeInsert() == -1;
            }
//            if(hasFail)
//                throw new RuntimeException("Something is wrong with insert!");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 場所をデータベースに書き込む。書き込み後、割り当てられたIDがs.idに格納される
     * @param db 書き込むデータベース
     * @param s 登録する場所
     */
    public void insertSpot(SQLiteDatabase db, Spot s) {
        ContentValues values = new ContentValues();
        //IDが設定されていたら場所を追加ではなく更新するようにする
        if(s.id >= 0)
            values.put(WiPSDBContract.Spots.ID, s.id);
        values.put(WiPSDBContract.Spots.NAME, s.name);
        values.put(WiPSDBContract.Spots.LATITUDE, s.latitude);
        values.put(WiPSDBContract.Spots.LONGITUDE, s.longitude);

        long insertedRowId = db.insertWithOnConflict(WiPSDBContract.Spots.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        int spotid = (int)db.compileStatement("SELECT " + WiPSDBContract.Spots.ID + " FROM " + WiPSDBContract.Spots.TABLE_NAME +
            " WHERE rowid = " + insertedRowId).simpleQueryForLong();

        s.id = spotid;
    }

    public void insertPattern(SQLiteDatabase db, Pattern p) {
        ContentValues values = new ContentValues();
        values.put(WiPSDBContract.Patterns.ACCESS_POINT_ID, p.apid);
        values.put(WiPSDBContract.Patterns.SPOT_ID, p.spotid);
        values.put(WiPSDBContract.Patterns.AVERAGE_LEVEL, p.averageLevel);
        values.put(WiPSDBContract.Patterns.SAMPLE_COUNT, p.sampleCount);

        long ret = db.insertOrThrow(WiPSDBContract.Spots.TABLE_NAME, null, values);
    }

    public void insertAPPatternMap(SQLiteDatabase db, Spot spot, Map<AccessPoint, Pattern> apMap) {
        //アクセスポイントのリストを書き込む
        AccessPoint [] aps = apMap.keySet().toArray(new AccessPoint[apMap.size()]);
        insertAccessPoints(db, aps);

        //パターン挿入SQL文を予めコンパイルしておく
        //FIX : データが既に存在した時はREPLACEにする
        SQLiteStatement insertPatternStatement =
                db.compileStatement("INSERT OR REPLACE INTO " + WiPSDBContract.Patterns.TABLE_NAME +
                        " ( " + WiPSDBContract.Patterns.SPOT_ID + ", " +
                                WiPSDBContract.Patterns.ACCESS_POINT_ID + ", " +
                                WiPSDBContract.Patterns.AVERAGE_LEVEL + ", " +
                                WiPSDBContract.Patterns.SAMPLE_COUNT +
                        ") VALUES ( ?, " +
                        "(SELECT " + WiPSDBContract.AccessPoints.ID + " FROM " + WiPSDBContract.AccessPoints.TABLE_NAME + " WHERE " + WiPSDBContract.AccessPoints.MAC_ADDRESS + " = ?), " +
                        "?, ?)");

        List<Map.Entry<AccessPoint, Pattern>> sortedApList = sortToList(apMap);

        db.beginTransaction();
        try {
            boolean hasFail = false;
            for(int i = 0; i < sortedApList.size(); i++) {
                Map.Entry<AccessPoint, Pattern> ap = sortedApList.get(i);
                insertPatternStatement.clearBindings();
                insertPatternStatement.bindLong(1, spot.id);
                insertPatternStatement.bindString(2, ap.getKey().mac);
                insertPatternStatement.bindDouble(3, ap.getValue().averageLevel);
                insertPatternStatement.bindLong(4, ap.getValue().sampleCount);

                hasFail = insertPatternStatement.executeInsert() == -1;
            }
            if(hasFail)
                throw new RuntimeException("Something is wrong with insert pattern!");
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    private List<Map.Entry<AccessPoint, Pattern>> sortToList(Map<AccessPoint, Pattern> apMap) {
        List<Map.Entry<AccessPoint, Pattern>> list = new ArrayList<Map.Entry<AccessPoint, Pattern>>(apMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<AccessPoint, Pattern>>() {
            @Override
            public int compare(Map.Entry<AccessPoint, Pattern> lhs, Map.Entry<AccessPoint, Pattern> rhs) {
                return Double.compare(rhs.getValue().averageLevel, lhs.getValue().averageLevel);
            }
        });
        return list;
    }

    public void dumpDatabase(String filepath) {
        File sddir = Environment.getExternalStorageDirectory();
        File dbfile = mContext.getDatabasePath(WiPSDBContract.DATABASE_NAME);

        try {
            if(!sddir.canWrite())
                return; //sdカードに書き込めない

            if(dbfile.exists()) {
                new File(sddir.getPath() + filepath).mkdirs();
                FileChannel src = new FileInputStream(dbfile).getChannel();
                FileChannel dst = new FileOutputStream(new File(sddir, filepath)).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }

        } catch (IOException e) {
            //TODO: DB書き込み失敗
        }
    }

    /**
     * 既存のDBファイルを読み取ってDBを書き換える。
     * @param filepath
     */
    public void inputDBFile(String filepath){
        File dbfile = mContext.getDatabasePath(WiPSDBContract.DATABASE_NAME);

        try {
            if(dbfile.exists()) {
                FileChannel dst = new FileOutputStream(dbfile).getChannel();
                FileChannel src = new FileInputStream(new File(filepath)).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }

        } catch (IOException e) {
            //TODO: DB書き込み失敗
        }
    }

    public Spot selectSpot(SQLiteDatabase db, int spotid) {
        Cursor cursor = db.query(WiPSDBContract.Spots.TABLE_NAME,
                new String[]{WiPSDBContract.Spots.NAME, WiPSDBContract.Spots.LONGITUDE, WiPSDBContract.Spots.LATITUDE},
                WiPSDBContract.Spots.ID + " = " + spotid, null, null, null, null);

        if(cursor.getCount() <= 0)
            return null;

        cursor.moveToFirst();
        Spot spot = new Spot(spotid, cursor.getString(0), cursor.getDouble(2), cursor.getDouble(1));
        return spot;
    }

    public List<Integer> selectSpotIDFromApID(SQLiteDatabase db, Pattern[] patterns) {
        StringBuilder inValues = new StringBuilder("(");
        for(int i = 0; i < patterns.length; i++) {
            inValues.append(patterns[i].apid);
            inValues.append(i == patterns.length-1 ? ')' : ',');
        }

        Cursor cursor = db.query(true, WiPSDBContract.Patterns.TABLE_NAME,
                new String[]{WiPSDBContract.Patterns.SPOT_ID},
                "apid IN " + inValues, null, null, null, null, null);

        cursor.moveToFirst();
        List<Integer> spotIds = new ArrayList<Integer>(cursor.getCount());
        for(int i = 0; i < cursor.getCount(); i++) {
            spotIds.add( cursor.getInt(0) );
            cursor.moveToNext();
        }
        return spotIds;
    }

    public HashMap<AccessPoint, Pattern> selectPatternFromSpotID(SQLiteDatabase db, int spotid) {
        HashMap<AccessPoint, Pattern> apMap = new HashMap<AccessPoint, Pattern>();

        final String ap_id = "ap." + WiPSDBContract.AccessPoints.ID;
        final String ap_mac = "ap." + WiPSDBContract.AccessPoints.MAC_ADDRESS;
        final String ap_ssid = "ap." + WiPSDBContract.AccessPoints.SSID;
        final String ap_frequency = "ap." + WiPSDBContract.AccessPoints.FREQUENCY;
        final String pattern_apid = "p." + WiPSDBContract.Patterns.ACCESS_POINT_ID;
        final String pattern_spotid = "p."+ WiPSDBContract.Patterns.SPOT_ID;
        final String pattern_avglevel = "p." + WiPSDBContract.Patterns.AVERAGE_LEVEL;
        final String pattern_samplecnt = "p." + WiPSDBContract.Patterns.SAMPLE_COUNT;

        Cursor cursor = db.rawQuery("SELECT " +
                ap_id + ','
                + ap_mac + ','
                + ap_ssid + ','
                + ap_frequency + ','
                + pattern_apid + ','
                + pattern_spotid + ','
                + pattern_avglevel + ','
                + pattern_samplecnt +
        " FROM " + WiPSDBContract.Patterns.TABLE_NAME  + " AS p INNER JOIN "
                + WiPSDBContract.AccessPoints.TABLE_NAME + " AS ap ON " + ap_id + '=' + pattern_apid +
        " WHERE " + pattern_spotid + '=' + spotid, null);

        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i++) {
            AccessPoint ap = new AccessPoint(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3));
            Pattern p = new Pattern(cursor.getInt(4), cursor.getInt(5), cursor.getDouble(6), cursor.getInt(7));
            apMap.put(ap, p);
            cursor.moveToNext();
        }
        cursor.close();

        return apMap;
    }

    public void lookupAndFillPatternApIDs(SQLiteDatabase db, Map<AccessPoint, Pattern> apMap) {
        SQLiteStatement selectStatement = db.compileStatement("SELECT " + WiPSDBContract.AccessPoints.ID +
             " FROM " + WiPSDBContract.AccessPoints.TABLE_NAME + " WHERE " +
            WiPSDBContract.AccessPoints.MAC_ADDRESS + " = ? ");

        for(Map.Entry<AccessPoint, Pattern> ap : apMap.entrySet()) {
            try {
                selectStatement.bindString(1, ap.getKey().mac);
                long apid = selectStatement.simpleQueryForLong();
                ap.getValue().apid = (int)apid;
            }
            catch (SQLiteDoneException e) {
                ap.getValue().apid = -1; //Apが存在しない、不明
            }
            finally {
                selectStatement.clearBindings();
            }
        }
    }

    public void deleteSpot(SQLiteDatabase db, long[] spotids) {
        db.beginTransaction();
        for (long id : spotids) {
            db.delete(WiPSDBContract.Spots.TABLE_NAME, WiPSDBContract.Spots.ID + " = ? ", new String[]{Long.toString(id)});
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<Spot> allSpot() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(WiPSDBContract.Spots.TABLE_NAME,
                new String[]{WiPSDBContract.Spots.ID, WiPSDBContract.Spots.NAME},
               null, null, null, null, null);

        cursor.moveToFirst();
        List<Spot> result = new ArrayList<Spot>();
        if(cursor.getCount() <= 0)
            return result;

        do {
            result.add(new Spot(cursor.getInt(0), cursor.getString(1), -1, -1));
        }while (cursor.moveToNext());
        db.close();
        return result;
    }
    public void deleteSpot(int spotID){
        SQLiteDatabase db = getWritableDatabase();
        deleteSpot(db, new long[]{spotID});
        db.close();
    }

    public void dumpToCsv(){
        List<Spot> spotList = allSpot();
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/WIPS/dumped.csv");
        file.getParentFile().mkdir();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "SHIFT-JIS");
            BufferedWriter bw = new BufferedWriter(osw);
            StringBuilder builder = new StringBuilder("Spot Name,");
            for(int i = 0; i < 10; i++){
                builder.append("MAC,RSSI,");
            }
            builder.append("\n");
            bw.write(builder.toString());
            for (Spot spot:spotList){
                builder = new StringBuilder(spot.name + ",");
                Map<AccessPoint, Pattern> patternMap = selectPatternFromSpotID(getReadableDatabase(), spot.id);
                for(Map.Entry<AccessPoint, Pattern> entry: patternMap.entrySet()){
                    builder.append(entry.getKey().mac + "," + entry.getValue().averageLevel + ",");
                }
                builder.append("\n");
                bw.write(builder.toString());
            }
            bw.close();
            MediaScannerConnection.scanFile(mContext, new String[]{file.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}