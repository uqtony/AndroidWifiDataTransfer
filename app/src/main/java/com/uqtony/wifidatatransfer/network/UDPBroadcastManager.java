package com.uqtony.wifidatatransfer.network;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPBroadcastManager {
    static final String TAG = "UDPBroadcastManager";
    static final int BroadcastSendPort = 6688;
    static final int BroadcastRecvPort = 6677;

    static UDPBroadcastManager udpBroadcastManager = null;
    static Object lock = new Object();
    Context context;
    InetAddress broadcastIP = null;

    DatagramSocket recvSocket;

    public static UDPBroadcastManager getInst(Context _context) {
        synchronized (lock) {
            if (udpBroadcastManager == null)
                udpBroadcastManager = new UDPBroadcastManager(_context);
        }
        return udpBroadcastManager;
    }

    private UDPBroadcastManager(Context _context) {
        this.context = _context;
        byte[] broadcastIPArray = UDPBroadcastIPFinder.findBroadcastIP(context);
        try {
            if (broadcastIPArray != null)
                broadcastIP = InetAddress.getByAddress(broadcastIPArray);
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    public void sendUDPBroadcast(String messageStr) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastIP, BroadcastSendPort);
            socket.send(sendPacket);
            Log.d(TAG, getClass().getName() + "Broadcast packet sent to: " + broadcastIP.getHostAddress());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }
}
