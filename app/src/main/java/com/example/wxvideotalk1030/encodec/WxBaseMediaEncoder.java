package com.example.wxvideotalk1030.encodec;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.example.wxvideotalk1030.egl.EglHelper;
import com.example.wxvideotalk1030.egl.WLEGLSurfaceView;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public abstract class WxBaseMediaEncoder {

    private static final String TAG = "WxBaseMediaEncodec";

    private Surface surface;
    private EGLContext eglContext;

    private int width, heigth;

    private MediaCodec videoEncodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferInfo;

    private MediaMuxer mediaMuxer;

    private WLEGLSurfaceView.WlGLRender wxGlRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    private OnMediaInfoListener onMediaInfoListener;

    public WxBaseMediaEncoder(Context context){

    }

    public void setWxGlRender(WLEGLSurfaceView.WlGLRender wxGlRender){
        this.wxGlRender = wxGlRender;
    }

    public void setmRenderMode(int mRenderMode){
        if(wxGlRender == null){
            throw new RuntimeException("must set render first");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener){
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public void startRecord(){
        if(surface != null && eglContext != null){

        }
    }

    public void initEncodec(EGLContext eglContext, String savePath, String mimeType, int width, int heigth){
        this.width = width;
        this.heigth = heigth;
        this.eglContext = eglContext;
        initMediaEncodec(savePath, mimeType,width,heigth);
    }

    private void initMediaEncodec(String savePath, String mimeType, int width, int heigth){
        try{
            mediaMuxer = new MediaMuxer(savePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initVideoEncodec(mimeType, width, heigth);
        }catch (IOException e){
            Log.d(TAG,"init MediaMuxer fail" + e.getMessage());
        }
    }

    private void initVideoEncodec(String mimeType, int width, int heigth){
        try{
            videoBufferInfo = new MediaCodec.BufferInfo();
            videoFormat = MediaFormat.createVideoFormat(mimeType, width, heigth);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,width * heigth *4);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);

            videoEncodec = MediaCodec.createDecoderByType(mimeType);
            videoEncodec.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

            surface = videoEncodec.createInputSurface();
        }catch (IOException e){
            Log.d(TAG,"create Decodec fail" + e.getMessage());
            videoEncodec = null;
            videoFormat = null;
            videoBufferInfo = null;
        }
    }

    static class WxEGLMediaThread extends Thread{
        private final String TAG = "WxEGLMediaThread";
        private WeakReference<WxBaseMediaEncoder> encoder;
        private EglHelper eglHelper;
        private Object object;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        public WxEGLMediaThread(WeakReference<WxBaseMediaEncoder> encoder){
            this.encoder = encoder;
        }

        @Override

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see #start()
         * @see #stop()
         * @see #Thread(ThreadGroup, Runnable, String)
         */
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(encoder.get().surface,encoder.get().eglContext);

            while (true){
                if(isExit){
                    release();
                    break;
                }

                if(isStart){
                    if(encoder.get().mRenderMode == RENDERMODE_WHEN_DIRTY){
                        synchronized (object){
                            try{
                                object.wait();
                            }catch (InterruptedException e){
                                Log.d(TAG,"Dirty mode interrupt" + e.getMessage());
                            }
                        }
                    }else if(encoder.get().mRenderMode == RENDERMODE_CONTINUOUSLY){
                        try{
                            Thread.sleep(1000/60);
                        }catch (InterruptedException e){
                            Log.d(TAG,"continuously mode interrupt "+ e.getMessage());
                        }
                    }else {
                        throw new RuntimeException("unknow Render Mode");
                    }
                }
                onCreate();
                onChange(encoder.get().width,encoder.get().heigth);
                onDraw();
                isStart = true;
            }
        }
        public void release(){

        }
    }


    public interface OnMediaInfoListener{
        void onMediaIime(int times);
    }
}
