package com.example.wxvideotalk1030.camera;

import android.content.Context;
import android.util.AttributeSet;

import com.example.wxvideotalk1030.egl.WLEGLSurfaceView;

public class WxCameraView extends WLEGLSurfaceView {
    public WxCameraView(Context context) {
        this(context,null);
    }

    public WxCameraView(Context context, AttributeSet attrs) {
        this(context, null,0);
    }

    public WxCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
