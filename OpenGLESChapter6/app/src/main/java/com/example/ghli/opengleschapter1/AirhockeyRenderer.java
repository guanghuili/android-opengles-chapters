package com.example.ghli.opengleschapter1;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.ghli.opengleschapter1.objects.Mallet;
import com.example.ghli.opengleschapter1.objects.Table;
import com.example.ghli.opengleschapter1.programs.ColorShaderProgram;
import com.example.ghli.opengleschapter1.programs.TextureShaderProgram;
import com.example.ghli.opengleschapter1.utils.TextureHelper;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by gh.li on 2017/8/3.
 */

public class AirhockeyRenderer implements GLSurfaceView.Renderer
{
    //定义一个16x16的透视矩阵
    private final float[] mProjectMatrix = new float[16];

    private Mallet mallet;
    private Table table;
    private TextureShaderProgram textureShaderProgram;
    private ColorShaderProgram colorShaderProgram;
    private int texture;

    private Context context;
    // command+option+L 格式化代码
    public AirhockeyRenderer(Context context)
    {
        this.context = context;

    }

    /**
     * Surface被创建后调用通常发生在应用程序第一次运行时,当设备被唤醒或者用户从其他activity切回来时这个方法也可能会被调用,以为这个方法可能被调用多次
     * @param gl10
     * @param eglConfig
     */
    @Override
    public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
        GLES20.glClearColor( 0.f, 0.f,  0.f,  0.f);

        mallet = new Mallet();
        table = new Table();

        textureShaderProgram = new TextureShaderProgram(context);

        colorShaderProgram = new ColorShaderProgram(context);

        texture = TextureHelper.loadTexture(context,R.mipmap.air_hockey_surface);
    }

    /**
     * 当绘制一帧时被调用。 我们一定要绘制点东西,即使只是清除屏幕,因为该方法执行返回后渲染缓冲区会被交换并显示到屏幕上。如果什么都没有可能会看到糟糕的闪烁效果
     * @param unused
     */
    public void onDrawFrame(GL10 unused) {
        // 重绘背景色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(mProjectMatrix,texture);
        table.bindData(textureShaderProgram);
        table.draw();


        colorShaderProgram.useProgram();

        colorShaderProgram.setUniforms(mProjectMatrix);
        mallet.bindData(colorShaderProgram);
        mallet.draw();


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
