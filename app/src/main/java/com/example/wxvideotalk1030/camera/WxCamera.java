package com.example.wxvideotalk1030.camera;


import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.wxvideotalk1030.Utils.DisPlayerUtil;

import java.io.IOException;
import java.util.List;

public class WxCamera {
    private static final String TAG = "WxCamera";

    private Camera camera;

    private SurfaceTexture surfaceTexture;

    private int width,heigth;

    public WxCamera(Context context){
        this.width = DisPlayerUtil.getScreenWidth(context);
        this.heigth = DisPlayerUtil.getScreenHeigth(context);
    }

    public void initCamera(SurfaceTexture surfaceTexture, int cameraId){
        this.surfaceTexture = surfaceTexture;

    }

    public void setCameraParm(int cameraId){
        try{
            camera = Camera.open(cameraId);
            camera.setPreviewTexture(surfaceTexture);
            Camera.Parameters parameters = camera.getParameters();

            parameters.setFlashMode("off");
            parameters.setPreviewFormat(ImageFormat.NV21);

            Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(size.width,size.height);

            size = getFitSize(parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(size.width,size.height);

            camera.setParameters(parameters);
            camera.startPreview();

        }catch (IOException e){
            Log.e(TAG,"open camera fail"+ e.getMessage());
        }
    }

    public void stopPreview(){
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void changeCamera(int cameraId){
        if(camera != null){
            stopPreview();
        }
        setCameraParm(cameraId);
    }
    private Camera.Size getFitSize(List<Camera.Size> sizes){
        if(width < heigth){
            int t = heigth;
            heigth = width;
            width = t;
        }

        for(Camera.Size size: sizes){
            if(1.0f * size.width / size.height == 1.0f * width / heigth){
                return size;
            }
        }
        return sizes.get(0);
    }
}
