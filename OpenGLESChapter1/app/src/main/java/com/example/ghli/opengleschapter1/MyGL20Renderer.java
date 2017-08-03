package com.example.ghli.opengleschapter1;


import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by gh.li on 2017/8/3.
 */

public class MyGL20Renderer implements GLSurfaceView.Renderer
{
    @Override
    public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
        GLES20.glClearColor(1f, 0.5f, 0.5f, 1.0f);
    }


    public void onDrawFrame(GL10 unused) {
        // 重绘背景色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }


    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
}
