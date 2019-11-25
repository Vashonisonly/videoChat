package com.example.wxvideotalk1030;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wxvideotalk1030.RtmpPush.WxPushVideo;

public class LivePushActivity extends AppCompatActivity {

    private WxPushVideo wxPushVideo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        wxPushVideo = new WxPushVideo();
    }

    public void startLivePush(View view) {
        wxPushVideo.initLivePush("rtmp://47.101.200.189/myapp/mystream");
    }

    public void stopLivePush(View view) {

    }
}
