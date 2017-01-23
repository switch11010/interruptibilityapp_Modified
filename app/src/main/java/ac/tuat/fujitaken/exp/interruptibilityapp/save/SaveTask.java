package ac.tuat.fujitaken.exp.interruptibilityapp.save;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.RowData;

/**
 *
 * Created by Komuro on 2015/11/23.
 */
public class SaveTask {

    private ScheduledExecutorService schedule = null;
    private Timer fileUpdateSchedule = null;
    private List<SaveData> data = new ArrayList<>();
    private Context context;

    private Runnable saveTask = new Runnable() {
        @Override
        public void run() {
            save();
        }
    };

    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            fileUpdate();
        }
    };

    public SaveTask(Context context){
        this.context = context;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "ファイル書き込みの権限がありません。", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void addData(SaveData saveData){
        data.add(saveData);
    }

    public boolean start(int period, TimeUnit timeUnit) {
        if(schedule == null){
            for(SaveData saveData: data){
                if(!initialize(saveData)){
                    return false;
                }
            }
            schedule = Executors.newSingleThreadScheduledExecutor();
            schedule.scheduleAtFixedRate(saveTask, period, period, timeUnit);

            fileUpdateSchedule = new Timer();
            Calendar dateTime = Calendar.getInstance();
            dateTime.setTime(new Date());
            dateTime.add(Calendar.DAY_OF_MONTH, 1);

            dateTime.clear(Calendar.AM_PM);
            dateTime.clear(Calendar.HOUR);
            dateTime.clear(Calendar.HOUR_OF_DAY);
            dateTime.clear(Calendar.MINUTE);
            dateTime.clear(Calendar.SECOND);
            dateTime.clear(Calendar.MILLISECOND);

            fileUpdateSchedule.schedule(updateTask, dateTime.getTime());

            return true;
        }
        return false;
    }

    private boolean fileUpdate(){
        if(schedule != null) {
            schedule.shutdownNow();
            schedule = null;
            fileUpdateSchedule.cancel();
            save();

            for (SaveData saveData : data) {
                saveData.updateFile();
                if (!initialize(saveData)) {
                    return false;
                }
            }
            start(3, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    public void stop(){
        if(schedule != null){
            schedule.shutdownNow();
            new Thread(saveTask).start();
            schedule = null;
            fileUpdateSchedule.cancel();
        }
    }

    protected boolean initialize(SaveData data) {
        boolean success = false;

        File file = data.getFile();

        //ファイル位置のディレクトリが存在していなければ作成
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (parent.mkdirs()) {
                return false;
            }
        }

        if (file.exists()) {
            if (file.canWrite()) {
                success = true;
            }
        }
        else {
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
                        bufferedWriter.write(data.getHeader());
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
        }
        return success;
    }

    private void save(){
        List<String> files = new ArrayList<>();

        if(Environment.getExternalStorageDirectory().getFreeSpace() > 500000000) {
            for (int i = 0; i < data.size(); i++) {
                SaveData saveData = data.get(i);
                if (saveData == null) {
                    data.remove(i);
                    i--;
                } else {
                    files.add(saveData.getFile().getPath());
                    save(saveData);
                }
            }

            MediaScannerConnection.scanFile(context, files.toArray(new String[files.size()]), null, null);
        }
    }

    /**
     * データの保存
     */
    private void save(SaveData saveData) {
        if (!saveData.rock) {
            int BUFFER = 500;
            FileOutputStream fileOutputStream = null;
            OutputStreamWriter outputStreamWriter = null;
            BufferedWriter bufferedWriter = null;

            //並列処理のためのデータの退避
            List<RowData> temp = saveData.refresh();
            File file = saveData.getFile();

            if (temp.size() > 0) {
                try {
                    fileOutputStream = new FileOutputStream(file, true);
                    outputStreamWriter = new OutputStreamWriter(fileOutputStream, "SHIFT-JIS");
                    bufferedWriter = new BufferedWriter(outputStreamWriter);

                    StringBuilder builder = new StringBuilder();
                    for (RowData line : temp) {
                        builder.append(line.getLine()).append("\n");
                        if (builder.length() >= BUFFER) {
                            bufferedWriter.write(builder.toString());
                            builder = new StringBuilder();
                        }
                    }
                    bufferedWriter.write(builder.toString());

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
    }
}
