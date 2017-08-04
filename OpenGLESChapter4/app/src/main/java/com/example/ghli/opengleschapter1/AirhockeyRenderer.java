package com.example.ghli.opengleschapter1;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.ghli.opengleschapter1.utils.ShareHelper;
import com.example.ghli.opengleschapter1.utils.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by gh.li on 2017/8/3.
 */

public class AirhockeyRenderer implements GLSurfaceView.Renderer
{

    // 每个float有32位(bit)精度 而每一个字节(bit)只有8位精度 每个浮点数占用4个字节
    private static final int BYTES_PPER_FLOAT = 4;
    // 每个顶点是一个坐标(x,y) 有两个分量
    private static final int POSITION_COMPONENT_COUNT = 2;
    // 每个颜色是(r,g,b)3分量
    private static final int COLOR_COMPONENT_COUNT = 3;

    private static final int STRIED =  (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PPER_FLOAT;
    //定义一个16x16的透视矩阵
    private final float[] mProjectMatrix = new float[16];
    //视图矩阵
    private final float[] mVMatrix= new float[16];
    //透视矩阵与视图矩阵变换后的总矩阵
    private final float[] mMVPMatrix= new float[16];

    private static final String A_COLOR = "a_Color";
    private static final String A_POSITION = "a_Position";
    private static final String U_MATRIX = "u_Matrix";

    private int aColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;


    // 在本地内存中存储数据
    private final FloatBuffer vertexData;

    private Context context;
    // command+option+L 格式化代码
    public AirhockeyRenderer(Context context)
    {
        this.context = context;

        // 屏幕中心点为 (0.f  0.f)  左下角为 (-1f,-1f) (x轴:左负右正 y轴:上正下负)
        // 桌子顶点坐标数组
        float[] tableVerticesWithTriangles =
                {
                        // 桌子数据 利用三角形扇(逆时针方向)
                        // 中心点
                        0.f, 0.f, 1.0f, 1.0f, 1.0f,//(x,y,r,g,b)
                        -0.5f, -0.8f, 0.7f, 0.7f, 0.7f, // 最终闭合点
                        0.5f, -0.8f, 0.7f, 0.7f, 0.7f,

                        0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                        -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                        -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,// 闭合点

                        // Line1 中间分割线
                        -0.5f, 0f,1.0f, 0.f, 0.f,
                        0.5f, 0f,1.0f, 0.f, 0.f,

                        // Mallerts 两个木槌(两个点)
                        0f, -0.25f,0.f, 0.f, 1.f,
                        0f, 0.25f,1.0f, 0.f, 0.f
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

        aColorLocation = GLES20.glGetAttribLocation(programId,A_COLOR);

        GLog.i("aColorLocation = "+ aColorLocation);

        aPositionLocation = GLES20.glGetAttribLocation(programId,A_POSITION);

        GLog.i("aPositionLocation = "+ aPositionLocation);

        uMatrixLocation = GLES20.glGetUniformLocation(programId,U_MATRIX);

        GLog.i("uMatrixLocation = "+ uMatrixLocation);

        vertexData.position(0);
        // 设置顶点数据 告诉OpenGL 它可以在 vertexData 寻找 a_Position 对应的数据
        GLES20.glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,GLES20.GL_FLOAT,false,STRIED,vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);


        vertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation,COLOR_COMPONENT_COUNT,GLES20.GL_FLOAT,false,STRIED,vertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);
    }

    /**
     * 当绘制一帧时被调用。 我们一定要绘制点东西,即使只是清除屏幕,因为该方法执行返回后渲染缓冲区会被交换并显示到屏幕上。如果什么都没有可能会看到糟糕的闪烁效果
     * @param unused
     */
    public void onDrawFrame(GL10 unused) {
        // 重绘背景色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,mProjectMatrix,0);

        // 由于为 a_Color 属性指定了颜色数据 所以不需要再调用  GLES20.glUniform4f(uColorLocation,0.0f,0.0f,1.f,1.0f); 设置颜色

        // 绘制桌子(利用三角形扇)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,6);

        // 绘制分割线
        GLES20.glDrawArrays(GLES20.GL_LINES,6,2);


        // 更新着色器的颜色设置
        // 绘制木槌点 需要在顶点着色器中指定点的大小 gl_PointSize = 10.0; gl_PointSize是以a_Position为中心点的四边形 当你设置gl_PointSize较大时可以看到效果
        GLES20.glDrawArrays(GLES20.GL_POINTS,8,1);

        GLES20.glDrawArrays(GLES20.GL_POINTS,9,1);

    }

    /**
     * 当Surface创建后 每次Surface尺寸发生变化时被调用。 在横竖屏来回切换时Surfacec尺寸会发生变化
     * @param unused
     * @param width
     * @param height
     */
    public void onSurfaceChanged(GL10 unused, int width, int height)
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



    }
}
