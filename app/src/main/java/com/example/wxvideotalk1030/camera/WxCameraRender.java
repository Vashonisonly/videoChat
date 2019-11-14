package com.example.wxvideotalk1030.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.example.wxvideotalk1030.R;
import com.example.wxvideotalk1030.egl.WLEGLSurfaceView;
import com.example.wxvideotalk1030.egl.WlShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class WxCameraRender implements WLEGLSurfaceView.WlGLRender,SurfaceTexture.OnFrameAvailableListener {
    private Context contex;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f,1f
    };

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private FloatBuffer vertexBuffer;

    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer fragmentBuffer;
    private int program;
    private int vPosition, fPosition;
    private int vboId;
    private int fboId;

    private int fboTextureId;
    private int cameraTextureId;

    private SurfaceTexture surfaceTexture;
    private OnSurfaceCreateListener onSurfaceCreateListener;

    public WxCameraRender(Context contex){
        this.contex = contex;

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length*4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length*4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragmentData);
        fragmentBuffer.position(0);
    }

    public void onSurfaceCreate(SurfaceTexture surfaceTexture) {
        onSurfaceCreateListener.onSurfaceCreate(surfaceTexture);
    }

    @Override
    public void onSurfaceCreated() {

        String vertexSource = WlShaderUtil.getRawResource(contex, R.raw.vertex_shader);
        String fragmentSource = WlShaderUtil.getRawResource(contex,R.raw.fragment_shader);

        program = WlShaderUtil.createProgram(vertexSource,fragmentSource);
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");

        //vbo
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1,vbos,0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length*4+fragmentData.length*4,null,GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0 ,vertexData.length*4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,vertexData.length*4,fragmentData.length*4,fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        //fbo
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1,fbos,0);
        fboId = fbos[0];

        GLES20.glBindBuffer(GLES20.GL_FRAMEBUFFER,fboId);

        //texture
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1,textureIds,0);
        fboTextureId = textureIds[0];

        GLES20.glBindBuffer(GLES20.GL_TEXTURE_2D, fboTextureId);

        //对参数有疑惑
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,GLES20.GL_RGBA,720,1280,0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,GLES20.GL_TEXTURE_2D,fboTextureId,0);
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE){
            Log.e("WangxinVideochat","fbo wrong");
        }else{
            Log.e("WangxinVideochat","fbo sucess");
        }

        GLES20.glBindBuffer(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

        //cameraTexture
        int[] textureideos = new int[1];
        GLES20.glGenTextures(1,textureideos,0);
        cameraTextureId = textureideos[0];

        //不太懂
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,cameraTextureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(cameraTextureId);
        surfaceTexture.setOnFrameAvailableListener(this);

        if(onSurfaceCreateListener != null){
            onSurfaceCreateListener.onSurfaceCreate(surfaceTexture);
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }

    @Override
    public void onDrawFrame() {

    }

    public interface OnSurfaceCreateListener{
        void onSurfaceCreate(SurfaceTexture surfaceTexture);
    }
}
