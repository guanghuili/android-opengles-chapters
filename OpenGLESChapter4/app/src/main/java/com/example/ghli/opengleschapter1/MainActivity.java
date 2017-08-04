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
 *     1. 解决横竖屏时 桌子被压扁的问题
 *     2. 利用正交投影修复该问题
 *     3. 矩阵向量如何相乘
 *     4. 正交投影: 不管多远或者多斤 所有物体看上去大小总是相同的(空中俯瞰火车轨道)
 *     5. 轴测投影: 从测角观察的一种正交投影
 *     6. 向量: 一个有多个元素的一维数组比如 一个位置和颜色通吃是一个四元素向量(x,y,z,w)(r,g,b,a)
 *     7. 矩阵: 一个矩阵是一个有多个元素的二维数组 一般用矩阵做向量投影 如:正交活透视投影并使用它们旋转,平移,缩放物体。 利用矩阵与每个要变换的向量相乘即可实现这些变换
 *
 * 适应宽高比解决思路:
 *      我们需要调整坐标空间,把屏幕的形状考虑在内,可行的一个方法是把较小的范围固定在【-1,1 】内 而按屏幕尺寸的比例调整较大的范围
 *      举例: 竖屏模式下 宽度为720 高度为1280 因此我们把较小的范围固定为【-1,1】并把高度范围调整为【-1280/720,1280/720】即【-1.78,1.78】
 *
 * uniform变量 : uniform变量是外部application程序传递给（vertex和fragment）shader的变量。因此它是application通过函数glUniform**（）函数赋值的。在（vertex和fragment）shader程序内部，uniform变量就像是C语言里面的常量（const ），
 *  它不能被shader程序修改。uniform变量一般用来表示：变换矩阵，材质，光照参数和颜色等信息
 *
 *  attribute变量 :  attribute变量是只能在vertex shader中使用的变量 它不能在fragment shader中声明attribute变量，也不能被fragment shader中使用）
 *   一般用attribute变量来表示一些顶点的数据，如：顶点坐标，法线，纹理坐标，顶点颜色等 <p>在application中，一般用函数glBindAttribLocation（）来绑定每个attribute变量的位置，然后用函数glVertexAttribPointer（）为每个attribute变量赋值。
 *
 *
 *   varying变量 :  varying变量是vertex和fragment shader之间做数据传递用的。一般vertex shader修改varying变量的值，然后fragment shader使用该varying变量的值。因此varying变量在vertex和fragment shader二者之间的声明必须是一致的。application不能使用此变量
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
