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
 *      顶点 : 一个订单就是一个几何图形的拐角点(这个点有很多附加属性 最重要的就是位置, 比如一个矩形有四个拐角 代表有四个顶点。每个顶点有一个坐标)
 *      顶点着色器: 生成每个顶点的最终位置,针对每个顶点都会执行一次。
 *      片段着色器: 为顶点组成的点,直线或者三角形的每个片段生成最终的颜色,针对每个片段都会执行一次。一个片段是一个小的,单一颜色的长方形区域,类似屏幕上的一个像素。
 *      光栅化: 把每个点,直线以及三角形分解成大量的小片段他们可以被映射到移动设备显示屏上的像素上,从而生成一幅图像,这些片段类似屏幕上的像素点。
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
