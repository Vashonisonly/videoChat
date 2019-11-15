package com.example.wxvideotalk1030.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.wxvideotalk1030.R;
import com.example.wxvideotalk1030.Utils.DisPlayerUtil;
import com.example.wxvideotalk1030.egl.WLEGLSurfaceView;
import com.example.wxvideotalk1030.egl.WlShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class WxCameraRender implements WLEGLSurfaceView.WlGLRender,SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "WxCameraRender";
    private Context contex;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f,1f
    };


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

    //
    private int umatrix;
    private float[] matrix = new float[16];

    private SurfaceTexture surfaceTexture;
    private OnSurfaceCreateListener onSurfaceCreateListener;

    private WxCameraFboRender wxCameraFboRender;

    private int screenWidth, screenHeigth;
    private int width, heigth;

    public WxCameraRender(Context contex){
        this.contex = contex;

        screenWidth = DisPlayerUtil.getScreenWidth(contex);
        screenHeigth = DisPlayerUtil.getScreenHeigth(contex);

        wxCameraFboRender = new WxCameraFboRender(contex);
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

    @Override
    public void onSurfaceCreated() {

        wxCameraFboRender.onCreate();
        String vertexSource = WlShaderUtil.getRawResource(contex, R.raw.vertex_shader);
        String fragmentSource = WlShaderUtil.getRawResource(contex,R.raw.fragment_shader);

        program = WlShaderUtil.createProgram(vertexSource,fragmentSource);
        Log.d(TAG,"surfaceCreated, program is " + program);
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        umatrix = GLES20.glGetUniformLocation(program,"u_Matrix");

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

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,fboId);

        //texture
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1,textureIds,0);
        fboTextureId = textureIds[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);

        //对参数有疑惑
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,GLES20.GL_RGBA,screenWidth,screenHeigth,0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,GLES20.GL_TEXTURE_2D,fboTextureId,0);
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE){
            Log.e("WangxinVideochat","fbo wrong");
        }else{
            Log.d("WangxinVideochat","fbo sucess");
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
            onSurfaceCreateListener.onSurfaceCreate(surfaceTexture, fboTextureId);
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0);
    }

    public void resetMatrix(){
        Matrix.setIdentityM(matrix, 0);
    }

    public void setAngle(float angle, float x, float y, float z){
        Matrix.rotateM(matrix, 0, angle, x, y, z);
    }
    public int getFboTextureId(){
        return fboTextureId;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.width = width;
        this.heigth = height;
    }

    @Override
    public void onDrawFrame() {
        //???
        surfaceTexture.updateTexImage();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f,1f,0f,1f);

        GLES20.glUseProgram(program);

        GLES20.glViewport(0,0,screenWidth,screenHeigth);
        GLES20.glUniformMatrix4fv(umatrix,1,false,matrix,0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,fboId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vboId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,8,0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition,2,GLES20.GL_FLOAT,false,8,vertexData.length*4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

        wxCameraFboRender.onChange(width,heigth);
        wxCameraFboRender.onDraw(fboTextureId);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener){
        this.onSurfaceCreateListener = onSurfaceCreateListener;
    }

    public interface OnSurfaceCreateListener{
        void onSurfaceCreate(SurfaceTexture surfaceTexture,int textureId);
    }
}
