package ac.tuat.fujitaken.exp.interruptibilityapp.interruption;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
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
    private DatagramSocket client = null;

    //s コンストラクタ
    //s MainService.onCreate() → InterruptTiming() で呼ばれる
    public UDPConnection(Context context) throws UnknownHostException {
        myIPAddress = "";
        AppSettings settings = Settings.getAppSettings();

        ipAddress = InetAddress.getByName(settings.getIpAddress());
        port = settings.getPort();
        id = settings.getId();
    }

    //s PC に何か要求を送信するっぽい（そのまま）
    //s InterruptTiming.eventTriggeredThread() で呼ばれる
    public boolean sendRequest(RowData data){
        if(client == null) {
            try {
                String sndStr = id + "," + data.getLine();
                DatagramPacket sendPacket = new DatagramPacket(sndStr.getBytes("UTF-8"), sndStr.length(), ipAddress, port);
                client = new DatagramSocket();
                Log.d("UDP(Send)", "Sending : " + sndStr);
                client.send(sendPacket);
                Log.d("UDP(Send)", "Sent : " + "(" +sendPacket.getData().length + " Bytes)");
            } catch (SocketException e) {
                Log.e("UDP.sendRequest", "失敗");  //s 追加
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                Log.e("UDP.sendRequest", "失敗");  //s 追加
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }


    public static void sendIP(int ip){
        new Thread(()-> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
            DatagramSocket ipSendingClient;
            try {
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
                myIPAddress = strIPAddress;
                sndStr += strIPAddress;

                InetAddress ipAddress = InetAddress.getByName(settings.getIpAddress());
                int port = settings.getPort();

                DatagramPacket sendPacket = new DatagramPacket(sndStr.getBytes("UTF-8"), sndStr.length(), ipAddress, port);
                ipSendingClient = new DatagramSocket();
                Log.d("UDP(IP)", "IP: " +settings.getIpAddress() + ", Port : " + port);
                Log.d("UDP(IP)", "Sending : " + sndStr);
                ipSendingClient.send(sendPacket);
                Log.d("UDP(IP)", "Sent : " + "(" +sendPacket.getData().length + " Bytes)");
                ipSendingClient.close();
            } catch (SocketException e) {
                Log.e("UDP.sendIP", "失敗");  //s 追加
                e.printStackTrace();
                return;
            } catch (IOException e) {
                Log.e("UDP.sendIP", "失敗");  //s 追加
                e.printStackTrace();
                return;
            }
            return;
        }).start();
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
                    Log.d("Notify", "スタート");
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

        Log.d("Notify", "強制通知");
        controller.normalNotify();
    }

    //s PC から何か受信するっぽい（そのまま）
    //s InterruptTiming.eventTriggeredThread() で this.sendRequest() の直後に呼ばれる
    public String receiveData(){
        String message = "";
        if(client != null) {
            byte receivedBuff[] = new byte[64];
            DatagramPacket receivedPacket = new DatagramPacket(receivedBuff, receivedBuff.length);

            try {
                client.setSoTimeout(Constants.UDP_TIMEOUT);
                Log.d("UDP(Receive)", "Receiving... (Buffer size is " + receivedBuff.length + " Bytes)");
                client.receive(receivedPacket);
                message = new String(receivedPacket.getData(), "UTF-8");
                message = message.substring(0, message.indexOf('\0'));
                Log.d("UDP(Receive)", "Received : " + message + "(" + message.getBytes("UTF-8").length + " Bytes)");
            } catch (IOException e) {
                Log.e("UDP.receiveData", "受信に失敗");  //s 追加
                e.printStackTrace();
            } finally {
                client.close();
                client = null;
            }
        }
        return message;
    }
}
