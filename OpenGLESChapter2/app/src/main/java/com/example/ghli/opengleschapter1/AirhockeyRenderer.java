package com.example.ghli.opengleschapter1;


import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.ghli.opengleschapter1.utils.ShareHelper;
import com.example.ghli.opengleschapter1.utils.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by gh.li on 2017/8/3.
 */

public class AirhockeyRenderer implements GLSurfaceView.Renderer
{
    // 每个顶点是一个坐标(x,y) 有两个分量
    private static final int POSITION_COMPONENT_COUNT = 2;

    // 每个float有32位(bit)精度 而每一个字节(bit)只有8位精度 每个浮点数占用4个字节
    private static final int BYTES_PPER_FLOAT = 4;

    private static final String U_COLOR = "u_Color";
    private int uColorLocation;

    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;


    // 在本地内存中存储数据
    private final FloatBuffer vertexData;

    private Context context;

    public AirhockeyRenderer(Context context)
    {
        this.context = context;

        // 屏幕中心点为 (0.f  0.f)  左下角为 (-1f,-1f) (x轴:左负右正 y轴:上正下负)
        // 桌子顶点坐标数组
        float[] tableVerticesWithTriangles =
                {
                        /**
                         * 逆时针顺序排列顶点,这被称为卷曲顺序可以优化性能
                        */
                        // Triangle1
                        -0.5f, -0.5f,
                        0.5f, 0.5f,
                        -0.5f, 0.5f,

                        // Triangle2
                        -0.5f, -0.5f,
                        0.5f, -0.5f,
                        0.5f, 0.5f,

                        // Line1 中间分割线
                        -0.5f, 0f,
                         0.5f, 0f,

                        // Mallerts 两个木槌(两个点)
                        0f, -0.25f,
                        0f, 0.25f,
                };

        // allocateDirect分配一块本地内存 不被垃圾回收器管理
        vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTES_PPER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(tableVerticesWithTriangles);
    }

    /**
     * Surface被创建后调用通常发生在应用程序第一次运行时,当设备被唤醒或者用户从其他activity切回来时这个方法也可能会被调用,以为这个方法可能被调用多次
     * @param gl10
     * @param eglConfig
     */
    @Override
    public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
        GLES20.glClearColor( 0.f, 0.f,  0.f,  0.f);

        String verextShareSource = TextResourceReader.readTextFileFromResource(this.context,R.raw.simple_vertex_shader);
        String fragmentShareSource = TextResourceReader.readTextFileFromResource(this.context,R.raw.simple_fragment_shader);


        int vertextShader = ShareHelper.compileVertexShader(verextShareSource);
        int fragmentShader = ShareHelper.compileFragmentShader(fragmentShareSource);

        int programId = ShareHelper.linkProgram(vertextShader,fragmentShader);

        GLES20.glUseProgram(programId);

        GLES20.glValidateProgram(programId);

        uColorLocation = GLES20.glGetUniformLocation(programId,U_COLOR);

        GLog.i("uColorLocation = "+ uColorLocation);

        aPositionLocation = GLES20.glGetAttribLocation(programId,A_POSITION);

        GLog.i("aPositionLocation = "+ aPositionLocation);

        vertexData.position(0);
        // 设置顶点数据 告诉OpenGL 它可以在 vertexData 寻找 a_Position 对应的数据
        GLES20.glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,GLES20.GL_FLOAT,false,0,vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

    }

    /**
     * 当绘制一帧时被调用。 我们一定要绘制点东西,即使只是清除屏幕,因为该方法执行返回后渲染缓冲区会被交换并显示到屏幕上。如果什么都没有可能会看到糟糕的闪烁效果
     * @param unused
     */
    public void onDrawFrame(GL10 unused) {
        // 重绘背景色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 更新着色器的颜色设置
        GLES20.glUniform4f(uColorLocation,1.0f,1.0f,1.f,1.0f);
        // 绘制桌子
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);


        // 更新着色器的颜色设置
        GLES20.glUniform4f(uColorLocation,1.0f,0.0f,0.f,1.0f);
        // 绘制分割线
        GLES20.glDrawArrays(GLES20.GL_LINES,6,2);


        // 更新着色器的颜色设置
        GLES20.glUniform4f(uColorLocation,0.0f,0.0f,1.f,1.0f);
        // 绘制木槌点 需要在顶点着色器中指定点的大小 gl_PointSize = 10.0; gl_PointSize是以a_Position为中心点的四边形 当你设置gl_PointSize较大时可以看到效果
        GLES20.glDrawArrays(GLES20.GL_POINTS,8,1);
        GLES20.glUniform4f(uColorLocation,0.0f,0.0f,1.f,1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS,9,1);

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
