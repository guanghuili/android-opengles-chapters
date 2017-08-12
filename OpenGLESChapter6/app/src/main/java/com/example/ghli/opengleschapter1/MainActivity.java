package com.example.ghli.opengleschapter1;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ghli.opengleschapter1.utils.TextResourceReader;

/**
 * 空气曲棍球游戏
 * 主要知识点:
 *     1. 什么是纹理 ?
 *          加单的说纹理就是一个图像或者照片，它们可以被加载进 OpenGL 中，一单开始使用纹理就要开始使用多个着色器程序
 *
 *          纹理是由许多小的纹理元素（texel）组成，他们是小块的数据。类似我们之前讨论的片段和像素。
 *          纹理不必是正方形 但是每个维度都应该是2幂（POT），原因是POT纹理适用于所有情况
 *
 *     2. 撰写把纹理加载进 OpenGL 的代码
 *
 *     3. 学习如何显示纹理，调整代码，使其支持多个着色器程序
 *
 *     4. 纹理的过滤模式以以及他们的使用场景
 *
 *
 */
public class MainActivity extends AppCompatActivity
{
    private GLSurfaceView mGLSurfaceView;

    private boolean renderSet  = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        GLog.i(TextResourceReader.readTextFileFromResource(this,R.raw.simple_vertex_shader));

        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);



        // 如果支持2.0
        if (isSupportsGL20())
        {
            mGLSurfaceView.setEGLContextClientVersion(2);

            // GLSurfaceView 会在一个单独的线程中执行渲染 默认为已显示设备的刷新频率不断渲染(onDrawFrame 不停调用), 可通过设置setRenderMode为请求刷新
            mGLSurfaceView.setRenderer(new AirhockeyRenderer(this));

            // 调用mGLSurfaceView.requestRender();方法可以实现手动刷新
            //mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


            renderSet = true;

        } else
        {
            GLog.i("onCreate: This devise dost not support OpenGL ES 2.0");
        }

        this.runOnDraw(new Runnable()
        {
            @Override
            public void run() {
                GLog.i("runOnDraw Thread : " + Thread.currentThread().getName());
            }
        });

    }

    /**
     * 在GL线程中执行
     * @param runable
     */
    private void runOnDraw(Runnable runable)
    {
        mGLSurfaceView.queueEvent(runable);
    }

    // command+option+L 格式化代码

    /**
     * 判断当前设备是否支持GL2.0
     *
     * @return
     */
    private boolean isSupportsGL20()
    {

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        return configurationInfo.reqGlEsVersion >= 0x20000;  // 无法在模拟上工作

    }

    // 有了他们surface视图才能正确暂停并继续后台渲染线程,同时释放和续用OpenGL上下文
    @Override
    protected void onPause()
    {
        super.onPause();

        if (renderSet)
            mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (renderSet)
            mGLSurfaceView.onResume();

    }
}
