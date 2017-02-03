package ac.tuat.fujitaken.exp.interruptibilityapp.interrupt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.RowData;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment;

/**
 * Created by KKomuro on 2017/01/11.
 */

public class UDPConnection {
    private InetAddress ipAddress;
    private int port, id;
    private DatagramSocket client = null;

    public UDPConnection(Context context) throws UnknownHostException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        ipAddress = InetAddress.getByName(preferences.getString(SettingFragment.IP_ADDRESS, ""));
        port = preferences.getInt(SettingFragment.PORT, 54613);
        id = preferences.getInt(SettingFragment.SP_ID, 10);
    }

    public boolean sendRequest(RowData data){
        if(client == null) {
            try {
                String sndStr = id + "," + data.getLine();
                DatagramPacket sendPacket = new DatagramPacket(sndStr.getBytes("UTF-8"), sndStr.length(), ipAddress, port);
                client = new DatagramSocket();
                Log.d("UDP", "Sending : " + sndStr);
                client.send(sendPacket);
                Log.d("UDP", "Sent : " + "(" +sendPacket.getData().length + " Bytes)");
            } catch (SocketException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public String receiveData(){
        String message = "";
        if(client != null) {
            byte receivedBuff[] = new byte[64];
            DatagramPacket receivedPacket = new DatagramPacket(receivedBuff, receivedBuff.length);

            try {
                client.setSoTimeout(Constants.MAIN_LOOP_PERIOD);
                Log.d("UDP", "Receiving... (Buffer size is " + receivedBuff.length + " Bytes)");
                client.receive(receivedPacket);
                message = new String(receivedPacket.getData(), "UTF-8");
                message = message.substring(0, message.indexOf('\0'));
                Log.d("UDP", "Received : " + message + "(" + message.getBytes("UTF-8").length + " Bytes)");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                client.close();
                client = null;
            }
        }
        return message;
    }
}
