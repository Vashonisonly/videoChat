package com.example.wxvideotalk1030.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.example.wxvideotalk1030.egl.WLEGLSurfaceView;

public class WxCameraView extends WLEGLSurfaceView {

    private static final String TAG = "WxCameraView";

    private WxCameraRender wxCameraRender;
    private WxCamera wxCamera;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int textureId = -1;

    public WxCameraView(Context context) {
        this(context,null);
    }

    public WxCameraView(Context context, AttributeSet attrs) {
        this(context, null,0);
    }

    public WxCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wxCameraRender = new WxCameraRender(context);
        wxCamera = new WxCamera(context);
        setRender(wxCameraRender);
        previewAngle(context);
        wxCameraRender
    }

    public void previewAngle(Context context){
        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        wxCameraRender.resetMatrix();
        Log.d(TAG,"angle: "+ angle);
        switch (angle){
            //研究一下
            case Surface.ROTATION_0:
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    wxCameraRender.setAngle(90,0,0,1);
                    wxCameraRender.setAngle(180,1,0,0);
                }else {
                    wxCameraRender.setAngle(90f,0f,0f,1f);
                }
                break;
            case Surface.ROTATION_90:
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    wxCameraRender.setAngle(180,0,0,1);
                    wxCameraRender.setAngle(180,0,1,0);
                }else {
                    wxCameraRender.setAngle(90f,0f,0f,1f);
                }
                break;
            case Surface.ROTATION_180:
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    wxCameraRender.setAngle(90,0,0,1);
                    wxCameraRender.setAngle(180,0,1,0);
                }else {
                    wxCameraRender.setAngle(-90f,0f,0f,1f);
                }
                break;
            case Surface.ROTATION_270:
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    wxCameraRender.setAngle(180,0,1,0);
                    //wxCameraRender.setAngle(180,1,0,0);
                }else {
                    wxCameraRender.setAngle(90f,0f,0f,1f);
                }
                break;
        }
    }

    public int getTextureId(){
        return textureId;
    }
}
