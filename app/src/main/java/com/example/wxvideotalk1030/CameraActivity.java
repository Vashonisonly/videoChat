package com.example.wxvideotalk1030;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wxvideotalk1030.camera.WxCameraView;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

    private WxCameraView wxCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        wxCameraView = findViewById(R.id.cameraview);
    }

    @Override
    public void onConfigurationChanged(Configuration newconfig){
        Log.d(TAG,"configChanged");
        super.onConfigurationChanged(newconfig);
        wxCameraView.previewAngle(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        wxCameraView.onDestory();
    }
}
