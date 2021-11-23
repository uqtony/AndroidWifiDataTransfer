package com.uqtony.wifidatatransfer.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class DataTransferClient {
    static final String TAG = DataTransferClient.class.getSimpleName();
    private Socket socket;

    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "10.0.2.2";
    Context context;

    public DataTransferClient(Context context){
        this.context = context;
    }

    public static Bitmap loadBitmapFromView(View v) {
        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width,
                v.getLayoutParams().height,
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.measure(View.MeasureSpec.makeMeasureSpec(v.getLayoutParams().width,
                View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(v.getLayoutParams().height,
                        View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.draw(c);

        return b;
    }

    public static Bitmap imageViewToBitmap(View view){
        ImageView imageView=(ImageView) view;//EditText et = (EditText) findViewById(R.id.EditText01);
        Bitmap bmp=((BitmapDrawable)imageView.getDrawable()).getBitmap(); //String str = et.getText().toString();
        return bmp;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        //bm.recycle();
        return resizedBitmap;
    }

    public void sendBitmapToServer(InetAddress serverAddr, int port, Bitmap bmp) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            //bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos); // compress to JPEG
            byte[] array = bos.toByteArray();
            initClientSocket(serverAddr, port);
            OutputStream out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeInt(array.length);
            dos.write(array, 0, array.length);
            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initClientSocket(InetAddress serverAddr, int port) {
        try {
            socket = new Socket(serverAddr, port);

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void saveBitmap(Bitmap bitmap) {
        FileOutputStream fOut;
        try {
            File dir = new File("/sdcard/");
            if (!dir.exists()) {
                dir.mkdir();
            }

            String tmp = "/sdcard/my.jpg";
            File f = new File(tmp);

            f.createNewFile();
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            try {
                fOut.flush();
                fOut.close();
                File file = new File(tmp);
                file.renameTo(new File("/sdcard/on.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
}
