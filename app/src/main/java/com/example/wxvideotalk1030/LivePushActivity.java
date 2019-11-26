package com.example.wxvideotalk1030;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wxvideotalk1030.RtmpPush.WxConnectListener;
import com.example.wxvideotalk1030.RtmpPush.WxPushVideo;

public class LivePushActivity extends AppCompatActivity {
    private static final String TAG = "LivePushActivity";

    private WxPushVideo wxPushVideo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        wxPushVideo = new WxPushVideo();
        wxPushVideo.setWxConnectListener(new WxConnectListener() {
            @Override
            public void onConnecting() {
                Log.d(TAG,"connecting the server......");
            }

            @Override
            public void onConnected() {
                Log.d(TAG,"connect to the server success!");
            }

            @Override
            public void onConnectFail(String msg) {
                Log.e(TAG,"connect to the server fail!");
            }
        });
    }

    public void startLivePush(View view) {
        wxPushVideo.initLivePush("rtmp://47.101.200.189/myapp/mystream");
    }

    public void stopLivePush(View view) {

    }
}
