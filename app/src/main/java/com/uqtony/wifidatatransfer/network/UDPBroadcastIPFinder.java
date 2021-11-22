package com.uqtony.wifidatatransfer.network;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static android.content.Context.WIFI_SERVICE;

public class UDPBroadcastIPFinder {
    static final String TAG = "UDPBroadcastIPFinder";

    public static byte[] findBroadcastIP(Context context) {
        byte[] ipArray = getLocalIPAddress();
        if (ipArray == null) {
            ipArray = getWifiIPAddress(context);
        }
        if (ipArray != null && ipArray.length == 4) {
            ipArray[3] = (byte) 255;
            return ipArray;
        }
        return  null;
    }

    public static byte[] getLocalIPAddress() {
        Enumeration enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Log.w(TAG, e);
        }
        if (enumeration != null) {
// 遍歷所用的網路介面
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = (NetworkInterface)enumeration.nextElement();// 得到每一個網路介面繫結的地址
                Enumeration inetAddresses = nif.getInetAddresses();
// 遍歷每一個介面繫結的所有ip
                if (inetAddresses != null)
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress ip = (InetAddress)inetAddresses.nextElement();
                        if (!ip.isLoopbackAddress()) {
                            String hostAddress = ip.getHostAddress();
                            //if (isIPv4Address(ip.getHostAddress()))
                            if(ip.getAddress().length == 4)
                                return ip.getAddress();
                        }
                    }
            }
        }
        return null;
    }// end

    public static byte[] getWifiIPAddress(Context context) {
        WifiManager wm = (WifiManager)context.getSystemService(WIFI_SERVICE);
        if (wm == null)
            return null;
        if (!wm.isWifiEnabled())
            return null;
        int wifiIp =  wm.getConnectionInfo().getIpAddress();
        byte[] ipArray = new byte[4];
        ipArray[0] = (byte)(wifiIp & 0xff);
        ipArray[1] = (byte)(wifiIp>>8 & 0xff);
        ipArray[2] = (byte)(wifiIp>>16 & 0xff);
        ipArray[3] = (byte)(wifiIp>>24 & 0xff);
        return ipArray;
    }
}

