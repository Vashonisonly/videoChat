package com.example.wxvideotalk1030;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMediaCodecPlayer();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void cameraPreview(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void initMediaCodecPlayer(){
        Log.i(TAG,"---------------------");
        //动态权限申请
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //申请权限，REQUEST_TAKE_PHOTO_PERMISSION是自定义的常量
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},2);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
            //Log.e(TAG,"can not find permission for open camera!");
            //return;
        }
        Log.d(TAG,"---------------------");
    }
}
