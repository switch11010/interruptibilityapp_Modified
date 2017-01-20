package ac.tuat.fujitaken.kk.test.testapplication.interrupt;

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

import ac.tuat.fujitaken.kk.test.testapplication.Constants;
import ac.tuat.fujitaken.kk.test.testapplication.ui.fragments.SettingFragment;

/**
 * Created by KKomuro on 2017/01/11.
 */

public class UDPConnection {
    private InetAddress ipAddress;
    private DatagramPacket sendPacket;
    private DatagramSocket client = null;

    public UDPConnection(Context context) throws UnknownHostException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        ipAddress = InetAddress.getByName(preferences.getString(SettingFragment.IP_ADDRESS, ""));
        String sndStr = preferences.getString(SettingFragment.SP_ID, "");
        sendPacket = new DatagramPacket(sndStr.getBytes(), sndStr.length(), ipAddress, preferences.getInt(SettingFragment.PORT, 54613));
    }

    public boolean sendRequest(){
        if(client == null) {
            try {
                client = new DatagramSocket();
                client.send(sendPacket);
                Log.d("UDP", "Send : " + "(" +sendPacket.getData().length + " Bytes)");
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
                client.receive(receivedPacket);
                message = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                Log.d("UDP", "Received : " + message + "(" +receivedPacket.getData().length + " Bytes)");
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
