package com.example.wxvideotalk1030;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wxvideotalk1030.camera.WxCameraView;
import com.example.wxvideotalk1030.encodec.WxBaseMediaEncoder;
import com.example.wxvideotalk1030.encodec.WxMediaEncodec;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

    private WxCameraView wxCameraView;
    private Button btnRecord;
    private Button btnTurnCamera;
    private WxMediaEncodec wxMediaEncodec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        bindWidgets();

        Log.d(TAG,"onCreate-------");
    }
    private void bindWidgets(){
        wxCameraView = findViewById(R.id.cameraview);
        btnRecord = findViewById(R.id.record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"click~~~~~~~~");
                if(wxMediaEncodec == null){
                    wxMediaEncodec = new WxMediaEncodec(CameraActivity.this,wxCameraView.getTextureId());
                    wxMediaEncodec.initEncodec(wxCameraView.getEglContext(), Environment.getExternalStorageDirectory() + "/wxmediarecord.mp4",
                            720,1280,48000, 2);
                    wxMediaEncodec.setOnMediaInfoListener(new WxBaseMediaEncoder.OnMediaInfoListener() {
                        @Override
                        public void onMediaTime(int times) {
                            Log.d(TAG,"time is "+ times);
                            //btnRecord.setText(times);
                        }
                    });
                    wxMediaEncodec.startRecord();
                    btnRecord.setText(Environment.getExternalStorageDirectory() + "/wxmediarecord.mp4");
                }else {
                    wxMediaEncodec.stopRecord();
                    btnRecord.setText("录制");
                    wxMediaEncodec = null;
                }
            }
        });

        btnTurnCamera = findViewById(R.id.turnCamera);
        btnTurnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wxCameraView.turnCamera();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newconfig){
        Log.d(TAG,"configChanged------");
        super.onConfigurationChanged(newconfig);
        wxCameraView.previewAngle(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        wxCameraView.onDestory();
        wxMediaEncodec.stopRecord();
    }

}
