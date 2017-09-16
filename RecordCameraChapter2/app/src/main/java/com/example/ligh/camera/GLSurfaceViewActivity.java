package com.example.ligh.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.example.ligh.camera.drawer.OESTextureDrawer;
import com.example.ligh.camera.encoder.VideoEncoder;
import com.example.ligh.camera.programs.OESTextureShaderProgram;
import com.example.ligh.camera.utils.GLog;
import com.example.ligh.camera.utils.TextureHelper;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import static android.content.ContentValues.TAG;

/**
 * Created by ligh on 2017/8/19.
 * 
 * 利用GLSurfaceView实现相机预览+录制 功能
 */
public class GLSurfaceViewActivity extends Activity
{
    //定义一个16x16的透视矩阵
    private final float[] mProjectMatrix = new float[16];

    private int mTextureId;

    private SurfaceTexture mSurfaceTexture;
    private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
    private boolean mFrameAvailable;

    Camera mCamera;
    EGLContext mEGLDisplayContext;

    private OESTextureShaderProgram mSharedProgram;
    private OESTextureDrawer mDrawer;
    private  MyGLSurfaceView myGLSurfaceView;

    // 默认视频宽高
    private int mWidth = 320;
    private int mHeight = 480;

    // 录制持续时间
    private static final long DURATION_SEC = 8;
    // 记录开始录制时间
    private long mStartWhen = 0;
    // 结束时间
    private long mDesiredEnd = 0;

    // 视频编码器
    private VideoEncoder mVideoEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(myGLSurfaceView = new MyGLSurfaceView(this));


    }

    @Override
    protected void onResume()
    {
        super.onResume();

        myGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    private void openCamera(int width,int height)
    {

        mCamera = getCameraInstance();

        Camera.Parameters parms = mCamera.getParameters();
        parms.setPreviewSize(width, height);

        for (Camera.Size size : parms.getSupportedPreviewSizes())
        {

            if (size.width == width && size.height == height) {
                parms.setPreviewSize(width, height);
                mCamera.setParameters(parms);
                return;
            }
        }

        Log.w(TAG, "Unable to set preview size to " + width + "x" + height);

        Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
        parms.setPreviewSize(ppsfv.width, ppsfv.height);
        mCamera.setParameters(parms);

        mCamera.startPreview();

    }

    public  Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    public void attchSurfaceTexture()
    {
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class MyGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer
    {
        public MyGLSurfaceView(Context context)
        {
            super(context);
            this.setEGLContextClientVersion(2);

            this.setRenderer(this);

            this.setRenderMode(RENDERMODE_WHEN_DIRTY);

        }

        private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture)
            {
                requestRender();
            }
        };


        // GLSurfaceView.Renderer
        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height)
        {

            mWidth = width;
            mHeight = height;

            mVideoEncoder.setOutputSize(mWidth,mHeight);

            GLES20.glViewport(0, 0, width, height);

            openCamera(mWidth,mHeight);
            attchSurfaceTexture();

            // 启动编码器
            mVideoEncoder.startRecording(mEGLDisplayContext);

            mStartWhen = System.nanoTime();
            mDesiredEnd = mStartWhen + DURATION_SEC * 1000000000L;

        }

        @Override
        public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig)
        {
            GLES20.glClearColor(1.f, 0.f,  0.f,  0.f);

            mEGLDisplayContext = EGL14.eglGetCurrentContext();

            mSharedProgram = new OESTextureShaderProgram(GLSurfaceViewActivity.this);
            mDrawer = new OESTextureDrawer();

            mTextureId = TextureHelper.gentextures();

            // 初始化编码器
            mVideoEncoder = new VideoEncoder(GLSurfaceViewActivity.this,mTextureId);

            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);

        }

        @Override
        public void onDrawFrame(GL10 gl10)
        {
            //必须调用
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // 更新纹理图像为从图像流中提取的最近一帧。
            // 只有在拥有该纹理的OpenGL ES上下文被绑定到线程之后调用。隐式绑定该纹理到GL_TEXTURE_EXTERNAL_OES纹理目标。
            // 必须调用

            mSurfaceTexture.updateTexImage();


            mSurfaceTexture.getTransformMatrix(mProjectMatrix);

            mSurfaceTexture.getTransformMatrix(mVideoEncoder.getProjectMatrix());


            mSharedProgram.useProgram();
            mSharedProgram.setUniforms(mProjectMatrix,mTextureId);
            mDrawer.bindData(mSharedProgram);
            mDrawer.draw();


            if (System.nanoTime() >= mDesiredEnd)
            {
                mVideoEncoder.drainEncoder(true);
                mCamera.stopPreview();
                mVideoEncoder.releaseEncoder();

                GLog.i("System.nanoTime() >=  mDesiredEnd");

            }else
            {
                mVideoEncoder.drainEncoder(false);
            }
        }

    }


}
