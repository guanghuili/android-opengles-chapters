package com.example.ligh.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import com.example.ligh.camera.drawer.OESTextureDrawer;
import com.example.ligh.camera.programs.OESTextureShaderProgram;
import com.example.ligh.camera.utils.GLog;
import com.example.ligh.camera.utils.TextureHelper;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ligh on 2017/8/19.
 * 
 * 利用GLSurfaceView实现相机预览功能
 */
public class GLSurfaceViewActivity extends Activity
{
    //定义一个16x16的透视矩阵
    private final float[] mProjectMatrix = new float[16];

    private int mTextureId;

    private OESTextureShaderProgram mSharedProgram;

    private OESTextureDrawer mDrawer;

    private  MyGLSurfaceView myGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(myGLSurfaceView = new MyGLSurfaceView(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        myGLSurfaceView.onResume();
    }

    private class MyGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer
    {
        Camera mCamera;

        SurfaceTexture mSurfaceTexture;

        public MyGLSurfaceView(Context context)
        {
            super(context);
            this.setEGLContextClientVersion(2);

            this.setRenderer(this);

            this.setRenderMode(RENDERMODE_WHEN_DIRTY);

        }


        private void openCamera()
        {
            try {
                mCamera = getCameraInstance();
               // mCamera.setDisplayOrientation(180);
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
            }catch (IOException e){

            }
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

            GLES20.glViewport(0, 0, width, height);

            final float aspectRatio = (width > height) ? (float)width / (float)height : (float)height / (float)width;

            // public static void orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far)
            if(width > height)
            {
                // Landscape
                Matrix.orthoM(mProjectMatrix,0,-aspectRatio,aspectRatio,-1f,1f,-1f,1f);

            }else
            {
                // Protrait
                Matrix.orthoM(mProjectMatrix,0,-1f,1f,-aspectRatio,aspectRatio,-1f,1f);
            }
            openCamera();

            GLog.i("onSurfaceChanged");


        }



        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig)
        {
            GLES20.glClearColor( 0.f, 0.f,  0.f,  0.f);

            mSharedProgram = new OESTextureShaderProgram(GLSurfaceViewActivity.this);
            mDrawer = new OESTextureDrawer();


            mTextureId = TextureHelper.gentextures();

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

            mSharedProgram.useProgram();
            mSharedProgram.setUniforms(mProjectMatrix,mTextureId);
            mDrawer.bindData(mSharedProgram);
            mDrawer.draw();

        }




    }
}
