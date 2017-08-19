package com.example.ligh.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by ligh on 2017/8/19.
 */

/*
 * 使用SurfaceView预览相机
 * SurfaceView是Android1.0(API level1)时期就存在的，虽然是继承于View，
 * 但是他包含一个Surface模块（简单地说Surface对应了一块屏幕缓冲区，每个window对应一个Surface，
 * 任何View都是画在Surface上的，传统的view共享一块屏幕缓冲区，所有的绘制必须在UI线程中进行），
 * 所以SurfaceView与普通View的区别就在于他的渲染在单独的线程的，这对于一些游戏、视频等性能相关的应用非常有益，
 * 因为它不会影响主线程对事件的响应。同时由于这个特性它的显示也不受View的属性控制，所以不能进行平移，缩放等变换，也不能放在其它ViewGroup中
 * ，一些View中的特性也无法使用。

*/
public class SurfaceViewActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        setContentView(new CameraView(this));
    }

    public class CameraView extends SurfaceView implements SurfaceHolder.Callback
    {

        private SurfaceHolder holder;
        private Camera mCamera;

        public CameraView(Context context) {
            this(context,null);
        }

        public CameraView(Context context, AttributeSet attrs)
        {
            super(context, attrs);

            holder = getHolder();
            holder.addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            try {
                mCamera = getCameraInstance();
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
            }catch (IOException e){

            }
        }

        public Camera getCameraInstance()
        {
            Camera c = null;
            try {
                c = Camera.open(); // attempt to get a Camera instance
            }
            catch (Exception e){
                // Camera is not available (in use or does not exist)
            }
            return c; // returns null if camera is unavailable
        }


        // SurfaceHolder.Callback

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
        }

        public void  releaseCamera()
        {
            mCamera.release();
        }
    }


}


