package com.example.ghli.opengleschapter1;


import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by gh.li on 2017/8/3.
 */

public class FirstOpenGLRenderer implements GLSurfaceView.Renderer
{
    /**
     * Surface被创建后调用通常发生在应用程序第一次运行时,当设备被唤醒或者用户从其他activity切回来时这个方法也可能会被调用,以为这个方法可能被调用多次
     * @param gl10
     * @param eglConfig
     */
    @Override
    public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig)
    {
        GLES20.glClearColor(1f, 0.5f, 0.5f, 1.f);

    }

    /**
     * 当绘制一帧时被调用。 我们一定要绘制点东西,即使只是清除屏幕,因为该方法执行返回后渲染缓冲区会被交换并显示到屏幕上。如果什么都没有可能会看到糟糕的闪烁效果
     * @param unused
     */
    public void onDrawFrame(GL10 unused) {
        // 重绘背景色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLog.i("FirstOpenGLRenderer : onDrawFrame");
    }

    /**
     * 当Surface创建后 每次Surface尺寸发生变化时被调用。 在横竖屏来回切换时Surfacec尺寸会发生变化
     * @param unused
     * @param width
     * @param height
     */
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
}
