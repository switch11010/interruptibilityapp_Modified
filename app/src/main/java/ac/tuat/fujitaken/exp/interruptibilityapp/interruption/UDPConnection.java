package ac.tuat.fujitaken.exp.interruptibilityapp.interruption;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.AppSettings;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings;

/**
 *
 * Created by KKomuro on 2017/01/11.
 */

public class UDPConnection {
    private InetAddress ipAddress;
    private int port, id;
    private static String myIPAddress = "";
    private static boolean receiver = true;
    //    private DatagramSocket client = null;
    private boolean client = true;

    private static BluetoothAdapter mBTAdapter = null;//Bluetooth通信を行うために必要な情報を格納する
    private static BluetoothDevice mBTDevice = null;//実際に通信を行うデバイスの情報を格納する
    private static BluetoothSocket mBTSocket = null;//ソケット情報を格納する
    private static OutputStream mOutputStream = null;//出力ストリーム



    private static String MacAddress = "98:3B:8F:F8:E5:98";//アルファベットは全て大文字出ないとエラーになる
    private static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";//通信規格がSPPであることを示す数字


    //s コンストラクタ
    //s MainService.onCreate() → InterruptTiming() で呼ばれる
    public UDPConnection(Context context) throws UnknownHostException {
        myIPAddress = "";
        AppSettings settings = Settings.getAppSettings();

        ipAddress = InetAddress.getByName(settings.getIpAddress());
        port = settings.getPort();
        id = settings.getId();
        BTConnect();
    }

    public void BTConnect(){
        //BTアダプタのインスタンスを取得
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        //相手先BTデバイスのインスタンスを取得
        mBTDevice = mBTAdapter.getRemoteDevice(MacAddress);
        //ソケットの設定
        try {
            mBTSocket = mBTDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            LogEx.e("UDP.sendRequest", "BT取得成功");
            //接続開始
            try {
                mBTSocket.connect();
                LogEx.e("UDP.sendRequest", "BT接続成功");
            } catch (IOException connectException) {
                try {
                    mBTSocket.close();
                    mBTSocket = null;
                    LogEx.e("UDP.sendRequest", connectException.getLocalizedMessage());
                    LogEx.e("UDP.sendRequest", "BT接続失敗");
                } catch (IOException closeException) {
                    LogEx.e("UDP.sendRequest", "BT接続失敗");
                }
            }
        } catch (IOException e) {
            LogEx.e("UDP.sendRequest", "BT取得失敗");
            mBTSocket = null;
        }
        return;
    }

    private static void Send(String str){
        try {
            //ここで送信
            //文字列を送信する
            mOutputStream = mBTSocket.getOutputStream();
            byte[] bytes =str.getBytes(StandardCharsets.UTF_8);
            mOutputStream.write(bytes);
        } catch (IOException e) {
            LogEx.e("UDP.sendRequest", "失敗");
            try{
                mBTSocket.close();
            }catch(IOException e1){/*ignore*/}
        }
        return;
    }

    //s PC に何か要求を送信するっぽい（そのまま）
    //s InterruptTiming.eventTriggeredThread() で呼ばれる
    public boolean sendRequest(RowData data){
        if(client == true) {
            Runnable sender = new Runnable() {
                @Override
                public void run() {
                    Random r = new Random();
                    String sndStr = (r.nextInt(2) + 1) + "," + id + "," + data.getLine();
                    LogEx.d("UDP(Send)", "Sending : " + sndStr);
                    Send(sndStr);
                }
            };
            Thread th = new Thread(sender);
            th.start();

////            Runnable sender = new Runnable() {
////                @Override
////                public void run() {
////                    Socket socket = null;
////                    try {
////                        socket = new Socket(ipAddress, port);
////                        Random r = new Random();
////                        String sndStr = (r.nextInt(2) + 1) + "," + id + "," + data.getLine();
////                        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
////                        pw.println(sndStr);
////                    } catch (UnknownHostException e) {
////                        LogEx.e("UDP.sendRequest", "失敗");
////                        e.printStackTrace();
////                    } catch (IOException e) {
////                        LogEx.e("UDP.sendRequest", "失敗");
////                        e.printStackTrace();
////                    }
////                    if (socket != null) {
////                        try {
////                            socket.close();
////                            socket = null;
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////                    }
////                }
////            };
//            Thread th = new Thread(sender);
//            th.start();
            //ny ランダムで話しかけ/アピールを送る
//                Random r = new Random();
//                String sndStr = (r.nextInt(2) + 1) + "," + id + "," + data.getLine();
//                DatagramPacket sendPacket = new DatagramPacket(sndStr.getBytes("UTF-8"), sndStr.length(), ipAddress, port);
//                client = new DatagramSocket();
//                LogEx.d("UDP(Send)", "Sending : " + sndStr);
//                client.send(sendPacket);
//                LogEx.d("UDP(Send)", "Sent : " + "(" +sendPacket.getData().length + " Bytes)");
//            } catch (SocketException e) {
//                LogEx.e("UDP.sendRequest", "失敗");  //s 追加
//                e.printStackTrace();
//                return false;
//            } catch (IOException e) {
//                LogEx.e("UDP.sendRequest", "失敗");  //s 追加
//                e.printStackTrace();
//                return false;
//            }
            return true;
        }
        return false;
    }

    public static void sendIP(int ip){
        Runnable sender = new Runnable() {
            @Override
            public void run() {
                AppSettings settings = Settings.getAppSettings();

                String sndStr = settings.getId() + ",MyIPAddress,";
                String strIPAddress =
                        ((ip >> 0) & 0xFF) + "." +
                                ((ip >> 8) & 0xFF) + "." +
                                ((ip >> 16) & 0xFF) + "." +
                                ((ip >> 24) & 0xFF);

                if(myIPAddress.equals(strIPAddress)){
                    return;
                }
                LogEx.d("UDP(Send)", "Sending : " + sndStr);
                Send(sndStr);
            }
        };
        Thread th = new Thread(sender);
        th.start();
//        new Thread(()-> {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//
//            }
//            DatagramSocket ipSendingClient;
//            try {
//                AppSettings settings = Settings.getAppSettings();
//
//                String sndStr = settings.getId() + ",MyIPAddress,";
//                String strIPAddress =
//                        ((ip >> 0) & 0xFF) + "." +
//                                ((ip >> 8) & 0xFF) + "." +
//                                ((ip >> 16) & 0xFF) + "." +
//                                ((ip >> 24) & 0xFF);
//
//                if(myIPAddress.equals(strIPAddress)){
//                    return;
//                }
//                myIPAddress = strIPAddress;
//                sndStr += strIPAddress;
//
//                InetAddress ipAddress = InetAddress.getByName(settings.getIpAddress());
//                int port = settings.getPort();
//
//                DatagramPacket sendPacket = new DatagramPacket(sndStr.getBytes("UTF-8"), sndStr.length(), ipAddress, port);
//                ipSendingClient = new DatagramSocket();
//                LogEx.d("UDP(IP)", "IP: " +settings.getIpAddress() + ", Port : " + port);
//                LogEx.d("UDP(IP)", "Sending : " + sndStr);
//                //ipSendingClient.send(sendPacket);
//                LogEx.d("UDP(IP)", "Sent : " + "(" +sendPacket.getData().length + " Bytes)");
//                ipSendingClient.close();
//            } catch (SocketException e) {
//                LogEx.e("UDP.sendIP", "失敗");  //s 追加
//                e.printStackTrace();
//                return;
//            } catch (IOException e) {
//                LogEx.e("UDP.sendIP", "失敗");  //s 追加
//                e.printStackTrace();
//                return;
//            }
//            return;
//        }).start();
    }

    static private DatagramSocket receiveClient;
/*
    public static void startReceive(){
        receiver = true;
        Thread thread = new Thread(()-> {
            AppSettings settings = Settings.getAppSettings();
            int port = settings.getPort()-1;

            try {
                receiveClient = new DatagramSocket(port);
                receiveClient.setSoTimeout(0);
                while (receiver) {
                    byte receivedBuff[] = new byte[64];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedBuff, receivedBuff.length);
                    LogEx.d("Notify", "スタート");
                    receiveClient.receive(receivedPacket);
                    String message = new String(receivedPacket.getData(), "UTF-8");
                    message = message.substring(0, message.indexOf('\0'));
                    notify(message);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (receiveClient != null) {
                    receiveClient.close();
                }
            }
        });
        thread.start();
    }

    public static void stop(){
        receiver = false;
        if(receiveClient != null){
            receiveClient.close();
        }
    }*/

    private static void notify(String message){
        NotificationController controller = NotificationController.getInstance();
        InterruptTiming timing = controller.getTiming();
        if(System.currentTimeMillis() - timing.getPrevTime() <= Constants.NOTIFICATION_INTERVAL){
            return;
        }

        LogEx.d("Notify", "強制通知");
        controller.normalNotify();
    }

    //s PC から何か受信するっぽい（そのまま）
    //s InterruptTiming.eventTriggeredThread() で this.sendRequest() の直後に呼ばれる
//    public String receiveData(){
//        String message = "";
//        if(client != null) {
//            byte receivedBuff[] = new byte[64];
//            DatagramPacket receivedPacket = new DatagramPacket(receivedBuff, receivedBuff.length);
//
//            try {
//                client.setSoTimeout(Constants.UDP_TIMEOUT);
//                LogEx.d("UDP(Receive)", "Receiving... (Buffer size is " + receivedBuff.length + " Bytes)");
//                client.receive(receivedPacket);
//                message = new String(receivedPacket.getData(), "UTF-8");
//                message = message.substring(0, message.indexOf('\0'));
//                LogEx.d("UDP(Receive)", "Received : " + message + "(" + message.getBytes("UTF-8").length + " Bytes)");
//            } catch (IOException e) {
//                LogEx.e("UDP.receiveData", "受信に失敗");  //s 追加
//                e.printStackTrace();
//            } finally {
//                client.close();
//                client = null;
//            }
//        }
//        return message;
//    }
}
