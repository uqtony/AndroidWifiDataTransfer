package com.uqtony.wifidatatransfer.network;

import static com.uqtony.wifidatatransfer.network.SSDP.SSDPConstants.HOST;
import static com.uqtony.wifidatatransfer.network.SSDP.SSDPConstants.NEWLINE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


public class SSDP extends Thread {
    static final String TAG = SSDP.class.getSimpleName();

    static final String LED_NAME = "com.uqtony.led.94x48";
    static final String ST_LED = "ST:"+LED_NAME;

    /**
     * Default IPv4 multicast address for SSDP messages
     */
    public static final String ADDRESS = "239.255.255.250";

    public static final String IPV6_LINK_LOCAL_ADDRESS = "FF02::C";
    public static final String IPV6_SUBNET_ADDRESS = "FF03::C";
    public static final String IPV6_ADMINISTRATIVE_ADDRESS = "FF04::C";
    public static final String IPV6_SITE_LOCAL_ADDRESS = "FF05::C";
    public static final String IPV6_GLOBAL_ADDRESS = "FF0E::C";

    public static final String ST = "ST";
    public static final String LOCATION = "LOCATION";
    public static final String NT = "NT";
    public static final String NTS = "NTS";

    /* Definitions of start line */
    public static final String SL_NOTIFY = "NOTIFY * HTTP/1.1";
    public static final String SL_MSEARCH = "M-SEARCH * HTTP/1.1";
    public static final String SL_OK = "HTTP/1.1 200 OK";

    /* Definitions of notification sub type */
    public static final String NTS_ALIVE = "ssdp:alive";
    public static final String NTS_BYEBYE = "ssdp:byebye";
    public static final String NTS_UPDATE = "ssdp:update";

    public class SSDPConstants {
        /* New line definition */
        public static final String ADDRESS = "239.255.255.250";
        public static final int PORT = 1900;
        public static final String SL_OK= "HTTP/1.1 200 OK";
        public static final String SL_M_SEARCH ="M-SEARCH * HTTP/1.1" ;
        public static final String HOST = "Host:" + ADDRESS + ";" + PORT;
        public static final String MAN = "Man: \"ssdp: discover\"";
        public static final String NEWLINE= "\r\n";
        public static final String ST_Product = "ST:urn:schemas-upp-org:device: Server: 1";
        public static final String Found = "ST=urn: schemas-upnp-org:device:";
        public static final String Root = "ST: urn:schemas-upp-org: device:Server: 1";
        public static final String ALL= "ST:miivii";
    }

    public class SSDPSearchMsg
    {
        private int mMX = 5; /* seconds to delay response
         */
        private String mST; /* Search target */
        public SSDPSearchMsg (String ST) {
            mST = ST;
        }
        public int getmMX() {
            return mMX;
        }
        public void setmMX(int mMX) {
            this.mMX = mMX;
        }
        public String getmST(){
            return mST;
        }
        public void setmST(String mST) {
            this.mST = mST;
        }

        /**
         * @ruturn 发送格式：
         * M-SEARCH * HTTP/1.1
         * Host:239.255.255.250:1900
         * Man:"ssdp:discover"
         * MX:5
         * STamiivii
         */
        @Override
        public String toString(){
            StringBuilder content = new StringBuilder();
            content.append (SSDPConstants.SL_M_SEARCH) .append (NEWLINE);
            content.append (HOST) . append (NEWLINE);
            content.append (SSDPConstants.MAN).append (NEWLINE);
            content.append ("MX:" + mMX) .append (NEWLINE);
            content.append (mST).append (NEWLINE);
            content.append (NEWLINE);
            return content. toString();
        }
    }

    public class SSDPSocket {

        private MulticastSocket multicastSocket;
        private DatagramSocket unicastSocket;

        private InetAddress inetAddress;
        public SSDPSocket () throws IOException {
//默认地址和端口：port：1900,address: 239.255.255.250
            multicastSocket = new MulticastSocket (SSDPConstants.PORT); // Bind some rana
            inetAddress = InetAddress.getByName (SSDPConstants.ADDRESS) ;
            multicastSocket.joinGroup(inetAddress);

            unicastSocket = new DatagramSocket(null);
            unicastSocket.setReuseAddress(true);
            unicastSocket.bind(new InetSocketAddress(Utils.getLocalV4Address(Utils.getActiveNetworkInterface()),1900));
        }
        /* Used to send SSDp packet */
        public void send(String data) throws IOException {
            DatagramPacket dp = new DatagramPacket (data.getBytes (), data. length(), inetAddress, SSDPConstants.PORT);
            multicastSocket.send(dp);
        }
        /* Used to receive SSDP packet */
//        public DatagramPacket receive() throws IOException {
//            byte[] buf = new byte[1024];
//            DatagramPacket dp = new DatagramPacket (buf, buf. length) ;
//            multicastSocket.receive(dp);
//            return dp;
//        }

        public DatagramPacket receive() throws IOException {
            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket (buf, buf. length) ;
            unicastSocket.receive(dp);
            return dp;
        }

        public void close() {
            if (multicastSocket != null) {
                multicastSocket.close();
            }
            if (unicastSocket != null){
                unicastSocket.close();
            }
        }
    }

    private SocketAddress mMulticastGroupAddress = new InetSocketAddress("239.255.255.250", 1900);
    private MulticastSocket mMulticastSocket;
    private DatagramSocket mUnicastSocket;

    private NetworkInterface mNetIf;

    private Context mContext;

    private boolean mRunning = false;
    private InetAddress hostIP = null;

    public SSDP(Context ctx) throws IOException {
        mContext = ctx;
        mNetIf = Utils.getActiveNetworkInterface();
    }

    @Override
    public synchronized void start() {
        mRunning = true;
        super.start();
    }

    @Override
    public void run() {
        try {
            mMulticastSocket = new MulticastSocket(1900);
            mMulticastSocket.setLoopbackMode(true);
            mMulticastSocket.joinGroup(mMulticastGroupAddress, mNetIf);

            mUnicastSocket = new DatagramSocket(null);
            mUnicastSocket.setReuseAddress(true);
            mUnicastSocket.bind(new InetSocketAddress(Utils.getLocalV4Address(mNetIf),1900));

        } catch (IOException e) {
            Log.e(TAG, "Setup SSDP failed.", e);
        }
        while(mRunning) {
            DatagramPacket dp = null;
            try {
                dp = receive();
                String startLine = parseStartLine(dp);
                if(startLine.equals(SL_MSEARCH)) {
                    String st = parseHeaderValue(dp, ST);

                    //if(st.contains("dial-multiscreen-org:service:dial:1")) {
                    if(st.contains(LED_NAME)){
                        Log.d(TAG, "Receive MSearch, prepare reply");
                        String responsePayload = "HTTP/1.1 200 OK\n" +
                                //"ST: urn:dial-multiscreen-org:service:dial:1\n"+
                                ST_LED+"\n"+
                                "HOST: 239.255.255.250:1900\n"+
                                "EXT:\n"+
                                "CACHE-CONTROL: max-age=1800\n"+
                                "LOCATION: http://"+Utils.getLocalV4Address(mNetIf).getHostAddress()+":8008/ssdp/device-desc.xml\n" +
                                "CONFIGID.UPNP.ORG: 7339\n" +
                                "BOOTID.UPNP.ORG: 7339\n" +
                                "USN: uuid:"+ Installation.id(mContext)+"\n\n";


                        DatagramPacket response = new DatagramPacket(responsePayload.getBytes(), responsePayload.length(), new InetSocketAddress(dp.getAddress(),dp.getPort()));
                        mUnicastSocket.send(response);

                        //Log.d(LOG_TAG, "Responding to "+ dp.getAddress().getHostAddress());
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "SSDP fail.", e);
            }
        }
        Log.e(TAG, "SSDP shutdown.");

    }// end of run

    public synchronized void shutdown() {
        mRunning = false;
    }

    public InetAddress getHostIP() {
        return hostIP;
    }

    private DatagramPacket receive() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        mMulticastSocket.receive(dp);

        return dp;
    }

    public static String parseHeaderValue(String content, String headerName) {
        Scanner s = new Scanner(content);
        s.nextLine(); // Skip the start line

        while (s.hasNextLine()) {
            String line = s.nextLine();
            int index = line.indexOf(':');
            if (index < 0)
                continue;
            String header = line.substring(0, index);
            if (headerName.equalsIgnoreCase(header.trim())) {
                return line.substring(index + 1).trim();
            }
        }

        return "";
    }

    public static String parseHeaderValue(DatagramPacket dp, String headerName) {
        return parseHeaderValue(new String(dp.getData()), headerName);
    }

    public static String parseStartLine(String content) {
        Scanner s = new Scanner(content);
        return s.nextLine();
    }

    public static String parseStartLine(DatagramPacket dp) {
        return parseStartLine(new String(dp.getData()));
    }

    private WifiManager.MulticastLock multicastLock;
    private List<String> listReceive = new ArrayList<String>();

    private void acquireMultiLock() {
        WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        multicastLock = wm.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();//使用后，需要及时关闭
    }

    /**
     * 释放组锁
     */
    private void releaseMultiLock() {
        if (null != multicastLock) {
            multicastLock.release();
        }
    }

     public interface MSearchListener{
        void onMSearchReply(InetAddress hostIP);
    }

    public void sendMSearchMessage(MSearchListener mSearchListener){
        acquireMultiLock();
        //SSDPSearchMsg searchMsg = new SSDPSearchMsg (SSDPConstants.ALL) ;
        SSDPSearchMsg searchMsg = new SSDPSearchMsg(ST_LED);
        InetAddress myAddress = Utils.getLocalV4Address(Utils.getActiveNetworkInterface());

        SSDPSocket sock = null;
        try {
            //发送
            sock = new SSDPSocket();
            sock. send (searchMsg.toString());
            Log.i(TAG,"要发送的消息为：" + searchMsg.toString());
            //接收
            listReceive.clear();
            while (true) {
                DatagramPacket dp = sock.receive(); // Here, I only receive the same packets I initially
                String c = new String(dp.getData()).trim();
                String st = parseHeaderValue(dp, ST);
                if (!st.contains(LED_NAME))
                    continue;
                String ip = dp.getAddress().toString().trim();
                hostIP = dp.getAddress();
                if (myAddress.equals(hostIP))
                    continue;
                if (mSearchListener != null)
                    mSearchListener.onMSearchReply(hostIP);
                Log.e(TAG,"接收到的消息为：\n" +c+"\n来源IP地址："+ ip);
                //接收时候一遍后，直接跳出;循环
                if (listReceive.contains (c))
                    break;
                else listReceive.add(c);
            }
            sock.close();
            releaseMultiLock();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        //昆示接收结果
        mainHandler.post (new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i< listReceive.size(); i++) {

                    sb.append (i).append("\r\t").append(listReceive.get(i)).append(NEWLINE).append("---------------").append(NEWLINE);
                }
                String s = sb. toString();
                Log.d (TAG, "result= " + s);
            }
        });
    }
}

