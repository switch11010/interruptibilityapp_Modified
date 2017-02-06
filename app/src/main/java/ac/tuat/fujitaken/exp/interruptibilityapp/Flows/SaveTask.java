package ac.tuat.fujitaken.exp.interruptibilityapp.Flows;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.SaveData;

/**
 *
 * Created by Komuro on 2015/11/23.
 */
public class SaveTask {

    private ScheduledExecutorService schedule = null;
    private List<SaveData> data = new ArrayList<>();
    private Context context;

    private Runnable saveTask = ()->{
        List<String> files = new ArrayList<>();
        File storage = Environment.getExternalStorageDirectory();

        if(storage.getFreeSpace() > Constants.STORAGE_FREE_SPACE_LIMITATION) {
            for (int i = 0; i < data.size(); i++) {
                SaveData saveData = data.get(i);
                if (saveData == null) {
                    data.remove(i);
                    i--;
                } else {
                    File saveDataFile = saveData.getFile();
                    files.add(saveDataFile.getPath());
                    save(saveData);
                }
            }

            MediaScannerConnection.scanFile(context, files.toArray(new String[files.size()]), null, null);
        }
    };

    public SaveTask(Context appContext){
        this.context = appContext;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (appContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast toast = Toast.makeText(appContext, "ファイル書き込みの権限がありません。", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    public void addData(SaveData saveData){
        data.add(saveData);
    }

    public boolean couldStart(int period, TimeUnit timeUnit) {
        if(schedule == null){
            for(SaveData saveData: data){
                if(!couldInitialize(saveData)){
                    return false;
                }
            }
            schedule = Executors.newSingleThreadScheduledExecutor();
            schedule.scheduleAtFixedRate(saveTask, period, period, timeUnit);

            Calendar dateTime = Calendar.getInstance();
            dateTime.setTime(new Date());
            dateTime.add(Calendar.DAY_OF_MONTH, 1);

            dateTime.clear(Calendar.AM_PM);
            dateTime.clear(Calendar.HOUR);
            dateTime.clear(Calendar.HOUR_OF_DAY);
            dateTime.clear(Calendar.MINUTE);
            dateTime.clear(Calendar.SECOND);
            dateTime.clear(Calendar.MILLISECOND);

            return true;
        }
        return false;
    }

    public void stop(){
        if(schedule != null){
            schedule.shutdownNow();
            Thread thread = new Thread(saveTask);
            thread.start();
            schedule = null;
        }
    }

    private boolean couldInitialize(SaveData saveData) {
        boolean success = false;

        File file = saveData.getFile();

        //ファイル位置のディレクトリが存在していなければ作成
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (parent.mkdirs()) {
                return false;
            }
        }

        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;
        try {

            /**
             * ファイルの作成と，書き込み可能か判定
             * 可能ならヘッダを書き込んで初期化
             */
            if (file.createNewFile()) {
                if(file.canWrite()) {
                    fileOutputStream = new FileOutputStream(file, false);
                    outputStreamWriter = new OutputStreamWriter(fileOutputStream, "SHIFT-JIS");
                    bufferedWriter = new BufferedWriter(outputStreamWriter);
                    bufferedWriter.write(saveData.getHeader());
                    bufferedWriter.newLine();
                    success = true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SAVEDATA", e.getMessage());
        } finally {

            //ファイルのクローズ処理
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return success;
    }

    /**
     * データの保存
     */
    private void save(SaveData saveData) {
        if(saveData.rock){
            return;
        }

        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;

        //並列処理のためのデータの退避
        List<RowData> temp = saveData.refresh();
        File file = saveData.getFile();
        if(temp.size() <= 0){
            return;
        }

        try {
            fileOutputStream = new FileOutputStream(file, true);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, "SHIFT-JIS");
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            for (RowData line : temp) {
                bufferedWriter.write(line.getLine());
                bufferedWriter.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            //ファイルのクローズ
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
