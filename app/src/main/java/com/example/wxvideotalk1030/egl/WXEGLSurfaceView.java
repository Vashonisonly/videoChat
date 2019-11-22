package com.example.wxvideotalk1030.egl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public abstract class WXEGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback{


    private Surface surface;
    private EGLContext eglContext;

    private WxEGLThread wxEGLThread;
    private WxGLRender wxGLRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;


    public WXEGLSurfaceView(Context context) {
        this(context, null);
    }

    public WXEGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WXEGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    public void setRender(WxGLRender wxGLRender) {
        this.wxGLRender = wxGLRender;
    }

    public void setRenderMode(int mRenderMode) {

        if(wxGLRender == null)
        {
            throw  new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext)
    {
        this.surface = surface;
        this.eglContext = eglContext;
    }

    public EGLContext getEglContext()
    {
        if(wxEGLThread != null)
        {
            return wxEGLThread.getEglContext();
        }
        return null;
    }

    public void requestRender()
    {
        if(wxEGLThread != null)
        {
            wxEGLThread.requestRender();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(surface == null)
        {
            surface = holder.getSurface();
        }
        wxEGLThread = new WxEGLThread(new WeakReference<WXEGLSurfaceView>(this));
        wxEGLThread.isCreate = true;
        wxEGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        wxEGLThread.width = width;
        wxEGLThread.height = height;
        wxEGLThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        wxEGLThread.onDestory();
        wxEGLThread = null;
        surface = null;
        eglContext = null;
    }

    public interface WxGLRender
    {
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onDrawFrame();
    }


    static class WxEGLThread extends Thread {

        private WeakReference<WXEGLSurfaceView> wxeglSurfaceViewWeakReference;
        private EglHelper eglHelper = null;
        private Object object = null;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        private int width;
        private int height;

        public WxEGLThread(WeakReference<WXEGLSurfaceView> wxeglSurfaceViewWeakReference) {
            this.wxeglSurfaceViewWeakReference = wxeglSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(wxeglSurfaceViewWeakReference.get().surface, wxeglSurfaceViewWeakReference.get().eglContext);

            while (true)
            {
                if(isExit)
                {
                    //释放资源
                    release();
                    break;
                }

                if(isStart)
                {
                    if(wxeglSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY)
                    {
                        synchronized (object)
                        {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(wxeglSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY)
                    {
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        throw  new RuntimeException("mRenderMode is wrong value");
                    }
                }


                onCreate();
                onChange(width, height);
                onDraw();

                isStart = true;


            }


        }

        private void onCreate()
        {
            if(isCreate && wxeglSurfaceViewWeakReference.get().wxGLRender != null)
            {
                isCreate = false;
                wxeglSurfaceViewWeakReference.get().wxGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height)
        {
            if(isChange && wxeglSurfaceViewWeakReference.get().wxGLRender != null)
            {
                isChange = false;
                wxeglSurfaceViewWeakReference.get().wxGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw()
        {
            if(wxeglSurfaceViewWeakReference.get().wxGLRender != null && eglHelper != null)
            {
                wxeglSurfaceViewWeakReference.get().wxGLRender.onDrawFrame();
                if(!isStart)
                {
                    wxeglSurfaceViewWeakReference.get().wxGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();

            }
        }

        private void requestRender()
        {
            if(object != null)
            {
                synchronized (object)
                {
                    object.notifyAll();
                }
            }
        }

        public void onDestory()
        {
            isExit = true;
            requestRender();
        }


        public void release()
        {
            if(eglHelper != null)
            {
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                wxeglSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEglContext()
        {
            if(eglHelper != null)
            {
                return eglHelper.getmEglContext();
            }
            return null;
        }

    }


}
