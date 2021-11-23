package com.uqtony.wifidatatransfer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DataTransferServer {
    static final String TAG = DataTransferServer.class.getSimpleName();
    private ServerSocket serverSocket;

    Handler updateConversationHandler;

    Thread serverThread = null;


    private ImageView imageView;//  private TextView text;

    public static final int SERVERPORT = 6000;

    public DataTransferServer(ImageView imageView) {
        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        this.imageView = imageView;
    }

    public void destroy() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private DataInputStream input;//private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                //this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

                InputStream in = this.clientSocket.getInputStream();
                this.input = new DataInputStream(in);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            System.out.println("hello");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] data;//String read = input.readLine();
                    int len= this.input.readInt();
                    data = new byte[len];
                    if (len > 0) {
                        this.input.readFully(data,0,data.length);
                    }
                        /*
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] data;
                        int length = 0;
                        while ((length = this.input.read(data))!=-1) {
                            out.write(data,0,length);
                        }
                           data=out.toByteArray();
                        */
                    Log.d(TAG, "Receive data: length="+len);
                    updateConversationHandler.post(new updateUIThread(data));//updateConversationHandler.post(new updateUIThread(read));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    class updateUIThread implements Runnable {
        private byte[] byteArray;//private String msg;

        public updateUIThread(byte[] array){    //public updateUIThread(String str) {
            this.byteArray=array;   //this.msg = str;
        }

        @Override
        public void run() {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray , 0, byteArray .length);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
            //text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
            DataTransferClient.saveBitmap(bitmap);
        }
    }
}
