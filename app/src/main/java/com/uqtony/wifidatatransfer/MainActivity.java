package com.uqtony.wifidatatransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.uqtony.wifidatatransfer.network.Utils;

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
    ImageView imageView, imageView2, imageView3, imageView4;
    DataTransferServer dataTransferServer;

    private void prepareUI() {
        if (!Utils.isWifiApEnabled(this)) {
            Utils.changeStateWifiAp(this, true);
            try {
                Thread.sleep(3000);
//                Intent intent = getIntent();
//                finish();
//                startActivity(intent);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
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
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);

        imageView.setClickable(false);
        imageView2.setClickable(false);
        imageView3.setClickable(false);
        imageView4.setClickable(false);


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

        imageView3.setOnClickListener(new View.OnClickListener() {
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

        imageView4.setOnClickListener(new View.OnClickListener() {
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
                                imageView3.setVisibility(View.VISIBLE);
                                imageView4.setVisibility(View.VISIBLE);


                                imageView.setClickable(true);
                                imageView2.setClickable(true);
                                imageView3.setClickable(true);
                                imageView4.setClickable(true);


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
        Bitmap resizeBitmap = DataTransferClient.getResizedBitmap(bitmap, 192, bitmap.getHeight()*192/bitmap.getWidth());
        DataTransferClient dataTransferClient = new DataTransferClient(this);
        dataTransferClient.sendBitmapToServer(ssdp.getHostIP(), 6000, resizeBitmap);
    }
}