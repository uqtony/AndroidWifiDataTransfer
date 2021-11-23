package com.uqtony.wifidatatransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.uqtony.wifidatatransfer.network.DataTransferClient;
import com.uqtony.wifidatatransfer.network.DataTransferServer;
import com.uqtony.wifidatatransfer.network.SSDP;
import com.uqtony.wifidatatransfer.network.UDPBroadcastManager;

import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepareUI();
    }

    SSDP ssdp;
    TextView msgTextView, hintTextView;
    Button testButton;
    ImageView imageView, imageView2;
    DataTransferServer dataTransferServer;

    private void prepareUI() {
        testButton = findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doMSearch();
            }
        });
        msgTextView = findViewById(R.id.msg_textview);
        hintTextView = findViewById(R.id.hint_textview);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        imageView.setClickable(false);
        imageView2.setClickable(false);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ssdp.getHostIP() != null){
                    Thread t = new Thread(){
                        @Override
                        public void run() {
                            snapshotAndSendToServer(v);
                        }
                    };
                    t.start();
                    return;
                }
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ssdp.getHostIP() != null){
                    Thread t = new Thread(){
                        @Override
                        public void run() {
                            snapshotAndSendToServer(v);
                        }
                    };
                    t.start();
                    return;
                }
            }
        });

        dataTransferServer = new DataTransferServer(imageView);

        try {
            ssdp = new SSDP(this);
            ssdp.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void doMSearch() {
        Log.d("MainActivity", "test");
        //UDPBroadcastManager.getInst(this).sendUDPBroadcast("test");
        Thread t = new Thread(){
            @Override
            public void run() {
                ssdp.sendMSearchMessage(new SSDP.MSearchListener() {
                    @Override
                    public void onMSearchReply(InetAddress hostIP) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //testButton.setText("Send");
                                imageView.setVisibility(View.VISIBLE);
                                imageView2.setVisibility(View.VISIBLE);
                                imageView.setClickable(true);
                                imageView2.setClickable(true);
                                hintTextView.setVisibility(View.VISIBLE);
                                msgTextView.setText(System.currentTimeMillis()/1000+"##Find device: "+hostIP.toString().trim()+"\n"
                                        +msgTextView.getText());
                            }
                        });
                    }
                });
            }
        };
        t.start();
    }// end doMSearch

    private void snapshotAndSendToServer(View view) {
        Bitmap bitmap = DataTransferClient.imageViewToBitmap(view);
        if (bitmap == null)
            return;
        Bitmap resizeBitmap = DataTransferClient.getResizedBitmap(bitmap, 192, 108);
        DataTransferClient dataTransferClient = new DataTransferClient(this);
        dataTransferClient.sendBitmapToServer(ssdp.getHostIP(), 6000, resizeBitmap);
    }
}