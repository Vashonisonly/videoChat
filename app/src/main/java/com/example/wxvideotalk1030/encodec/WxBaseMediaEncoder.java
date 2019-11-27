package com.example.wxvideotalk1030.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.example.wxvideotalk1030.Audio.WxBaseAudioRecord;
import com.example.wxvideotalk1030.egl.EglHelper;
import com.example.wxvideotalk1030.egl.WXEGLSurfaceView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

public abstract class WxBaseMediaEncoder {

    private static final String TAG = "WxBaseMediaEncodec";

    private Surface surface;
    private EGLContext eglContext;

    private int width, heigth;

    private MediaCodec videoEncodec;
    private MediaCodec audioEncodec;

    private MediaFormat videoFormat;
    private MediaFormat audioFormat;

    private MediaCodec.BufferInfo videoBufferInfo;
    private MediaCodec.BufferInfo audioBufferInfo;

    private long audioPts = 0;
    private int sampleRate;

    private MediaMuxer mediaMuxer;
    private boolean videoExit;
    private boolean audioExit;
    private boolean encodecStart;

    private WxBaseAudioRecord wxBaseAudioRecord;

    private WxEGLMediaThread wxEGLMediaThread;
    private VideoEncodecThread videoEncodecThread;
    private AudioEncodecThread audioEncodecThread;

    private WXEGLSurfaceView.WxGLRender wxGlRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    private OnMediaInfoListener onMediaInfoListener;

    public WxBaseMediaEncoder(Context context){

    }

    public void setWxGlRender(WXEGLSurfaceView.WxGLRender wxGlRender){
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
            audioPts = 0;
            audioExit = false;
            videoExit = false;
            encodecStart = false;

            wxEGLMediaThread = new WxEGLMediaThread(new WeakReference<WxBaseMediaEncoder>(this));
            videoEncodecThread = new VideoEncodecThread(new WeakReference<WxBaseMediaEncoder>(this));
            audioEncodecThread = new AudioEncodecThread(new WeakReference<WxBaseMediaEncoder>(this));
            wxEGLMediaThread.isCreate = true;
            wxEGLMediaThread.isChange = true;
            wxEGLMediaThread.start();
            videoEncodecThread.start();
            audioEncodecThread.start();
        }
    }

    public void stopRecord(){
        // 是否不够严谨
        if(wxEGLMediaThread != null && videoEncodecThread != null){
            videoEncodecThread.exit();
            audioEncodecThread.exit();
            wxEGLMediaThread.onDestoryed();
            videoEncodecThread = null;
            wxEGLMediaThread = null;
        }
    }



    public void putPCMData(byte[] buffer,int size){
        if(audioEncodecThread != null && !audioEncodecThread.isExit && buffer != null && size > 0){
            int inputBufferIndex = audioEncodec.dequeueInputBuffer(0);
            if(inputBufferIndex >= 0){
                // *
            }
        }
    }

    // audio encodec 的音源输入在这里
    private void setupAudioRecordAndPutData(){
        wxBaseAudioRecord = new WxBaseAudioRecord();
        wxBaseAudioRecord.startRecord();
        wxBaseAudioRecord.setOnRecordListener(new WxBaseAudioRecord.OnRecordListener() {
            @Override
            public void readByte(byte[] audioData, int readSize) {
                //Log.d(TAG,"audio put data[10]" + audioData[10]);
                    if(audioEncodecThread != null && !audioEncodecThread.isExit && audioData != null && readSize > 0){
                        int inputBufferIndex = audioEncodec.dequeueInputBuffer(0);
                        if(inputBufferIndex >= 0){
                            ByteBuffer byteBuffer = audioEncodec.getInputBuffer(inputBufferIndex);
                            byteBuffer.clear();
                            byteBuffer.put(audioData);
                            long pts = getAudioPts(readSize,sampleRate);
                            audioEncodec.queueInputBuffer(inputBufferIndex,0,readSize,pts,0);
                        }
                    }
                }
        });
    }

    private long getAudioPts(int size, int sampleRate){
        audioPts += (long)(1.0 * size/(sampleRate * 2 * 2) * 1000000.0);
        return audioPts;
    }
    public void initEncodec(EGLContext eglContext, String savePath, int width, int heigth,int sampleRate, int channelCount){
        this.width = width;
        this.heigth = heigth;
        this.eglContext = eglContext;
        initMediaEncodec(savePath,width,heigth,sampleRate,channelCount);
    }

    private void initMediaEncodec(String savePath, int width, int heigth,int sampleTate, int channelCount){
        try{
            mediaMuxer = new MediaMuxer(savePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            Log.d(TAG,"开启录制，存放路径： "+savePath);
            initVideoEncodec(MediaFormat.MIMETYPE_VIDEO_AVC, width, heigth);
            initAudioEncodec(MediaFormat.MIMETYPE_AUDIO_AAC,sampleTate,channelCount);
        }catch (IOException e){
            Log.d(TAG,"init MediaMuxer fail " + e.getMessage());
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

            videoEncodec = MediaCodec.createEncoderByType(mimeType);
            Log.d(TAG,"videoFormat is "+ videoFormat);
            videoEncodec.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            Log.d(TAG,"videoEncodec is "+ videoEncodec);
            surface = videoEncodec.createInputSurface();
        }catch (IOException e){
            Log.d(TAG,"init VideoDecodec fail" + e.getMessage());
            videoEncodec = null;
            videoFormat = null;
            videoBufferInfo = null;
        }
    }

    private void initAudioEncodec(String mimeType, int sampleRate, int channelCount){
        try{
            this.sampleRate = sampleRate;
            audioBufferInfo = new MediaCodec.BufferInfo();
            audioFormat = MediaFormat.createAudioFormat(mimeType,sampleRate,channelCount);
            audioFormat.setString(MediaFormat.KEY_MIME,mimeType);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);// for waht?
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE,sampleRate);
            // 可以计算得出更好，目前写死
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,15*1024);
            audioEncodec = MediaCodec.createEncoderByType(mimeType);
            audioEncodec.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        }catch (IOException e){
            Log.d(TAG,"initAudioEncodec fail "+e.getMessage());
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
                //Log.d(TAG,"encodec loop");
                onCreate();
                onChange(encoder.get().width,encoder.get().heigth);
                onDraw();
                isStart = true;
            }
        }
        public void onCreate(){
            if(isCreate && encoder.get().wxGlRender != null){
                isCreate = false;
                encoder.get().wxGlRender.onSurfaceCreated();
            }
        }
        public void onChange(int width, int heigth){
            if(isChange && encoder.get().wxGlRender != null){
                isChange = false;
                encoder.get().wxGlRender.onSurfaceChanged(width,heigth);
            }
        }
        private void onDraw(){
            //Log.d(TAG,"encodec onDraw");
            if(encoder.get().wxGlRender != null && eglHelper != null){
                //Log.d(TAG,"encodec onDraw1");
                encoder.get().wxGlRender.onDrawFrame();
                if(!isStart){
                    //Log.d(TAG,"encodec onDraw2");
                    encoder.get().wxGlRender.onDrawFrame();
                }
                eglHelper.swapBuffers();
            }
        }
        private void requestRender(){
            if(object != null){
                synchronized (object){
                    object.notifyAll();
                }
            }
        }
        public void onDestoryed(){
            isExit = true;
            requestRender();
        }
        public void release(){
            if(eglHelper != null){
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                encoder = null;
            }
        }

    }

    static class VideoEncodecThread extends Thread{
        private WeakReference<WxBaseMediaEncoder> encoder;

        private boolean isExit;

        private MediaCodec videoEncodec;
        private MediaFormat videoFormat;
        private MediaCodec.BufferInfo videoBufferInfo;
        private MediaMuxer mediaMuxer;

        private int videoTrackIndex;
        private long pts;

        public VideoEncodecThread(WeakReference<WxBaseMediaEncoder> encoder){
            this.encoder = encoder;
            videoEncodec = encoder.get().videoEncodec;
            videoFormat = encoder.get().videoFormat;
            videoBufferInfo = encoder.get().videoBufferInfo;
            mediaMuxer = encoder.get().mediaMuxer;
            videoTrackIndex = -1;
        }

        @Override
        public void run(){
            super.run();
            pts = 0;
            videoTrackIndex = -1;
            isExit = false;

            videoEncodec.start();
            while (true){
                if(isExit){
                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;
                    encoder.get().videoExit = true;
                    if(encoder.get().audioExit){
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;
                    }
                    Log.d(TAG,"录制完成");
                    break;
                }

                //Log.d(TAG,"videoBufferInfo is " + videoBufferInfo);
                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferInfo,0);
                /*try{
                    currentThread().sleep(1000);
                }catch (InterruptedException e){
                    Log.d(TAG,e.getMessage());
                }*/
                Log.d(TAG,"outputbufferIndex: " + outputBufferIndex);
                if(outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    videoFormat = videoEncodec.getOutputFormat();
                    videoTrackIndex = mediaMuxer.addTrack(videoEncodec.getOutputFormat());
                    if(encoder.get().audioEncodecThread.audioTrackIndex != -1){
                        mediaMuxer.start();
                        encoder.get().encodecStart = true;
                    }
                }else {
                    // without wait(-1)
                    while (outputBufferIndex >= 0){
                        if(encoder.get().encodecStart){
                            ByteBuffer outputBuffer = videoEncodec.getOutputBuffer(outputBufferIndex);
                            outputBuffer.position(videoBufferInfo.offset);
                            outputBuffer.limit(videoBufferInfo.offset + videoBufferInfo.size);

                            if(pts == 0){
                                pts = videoBufferInfo.presentationTimeUs;
                            }
                            //???
                            videoBufferInfo.presentationTimeUs -= pts;
                            mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, videoBufferInfo);
                            if(encoder.get().onMediaInfoListener != null){
                                encoder.get().onMediaInfoListener.onMediaTime((int)(videoBufferInfo.presentationTimeUs/1000));
                            }
                        }
                        videoEncodec.releaseOutputBuffer(outputBufferIndex,false);
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferInfo,0);
                    }
                }
            }
        }
        public void exit(){
            isExit = true;
        }
    }

    static class AudioEncodecThread extends Thread{
        private WeakReference<WxBaseMediaEncoder> encoder;

        private boolean isExit;
        private MediaCodec audioEncodec;
        private MediaCodec.BufferInfo audioBufferInfo;
        private MediaMuxer mediaMuxer;

        private int audioTrackIndex = -1;
        long pts;

        public AudioEncodecThread(WeakReference<WxBaseMediaEncoder> encoder){
            this.encoder = encoder;
            audioEncodec = encoder.get().audioEncodec;
            audioBufferInfo = encoder.get().audioBufferInfo;
            mediaMuxer = encoder.get().mediaMuxer;
            audioTrackIndex = -1;
        }

        @Override
        public void run(){
            super.run();
            pts = 0;
            isExit = false;
            audioEncodec.start();
            // 开启录音
            encoder.get().setupAudioRecordAndPutData();
            while (true){
                if(isExit){
                    audioEncodec.stop();
                    audioEncodec.release();
                    audioEncodec = null;
                    encoder.get().audioExit = true;
                    if(encoder.get().videoExit){
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;
                    }
                    break;
                }

                int outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferInfo,0);
                if(outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    if(mediaMuxer != null){
                        audioTrackIndex = mediaMuxer.addTrack(audioEncodec.getOutputFormat());
                        if(encoder.get().videoEncodecThread.videoTrackIndex != -1){
                            mediaMuxer.start();
                            encoder.get().encodecStart = true;
                        }
                    }
                }
                else {
                    while (outputBufferIndex >= 0){
                        if(encoder.get().encodecStart){
                            ByteBuffer outputBuffer = audioEncodec.getOutputBuffer(outputBufferIndex);
                            outputBuffer.position(audioBufferInfo.offset);
                            outputBuffer.limit(audioBufferInfo.offset + audioBufferInfo.size);
                            if(pts == 0){
                                pts = audioBufferInfo.presentationTimeUs;
                            }
                            audioBufferInfo.presentationTimeUs = audioBufferInfo.presentationTimeUs - pts;
                            mediaMuxer.writeSampleData(audioTrackIndex,outputBuffer,audioBufferInfo);
                            if(encoder.get().onMediaInfoListener != null){
                                encoder.get().onMediaInfoListener.onMediaTime((int)(audioBufferInfo.presentationTimeUs/1000));
                            }
                        }
                        audioEncodec.releaseOutputBuffer(outputBufferIndex,false);
                        outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferInfo, 0);
                    }
                }
            }
        }

        public void exit(){
            isExit = true;
        }
    }


    public interface OnMediaInfoListener{
        void onMediaTime(int times);
    }
}
