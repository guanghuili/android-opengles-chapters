package com.example.ghli.opengleschapter1.objects;

/**
 * Created by ligh on 2017/8/12.
 */

import android.opengl.GLES20;

import com.example.ghli.opengleschapter1.Constants;
import com.example.ghli.opengleschapter1.data.VertexArray;
import com.example.ghli.opengleschapter1.programs.TextureShaderProgram;

/**
 * 桌子类 存储桌子的数据类  存储桌子的位置数据还会加入纹理坐标
 */
public class Table
{
    // 桌子在屏幕的位置坐标
    private static final int POSITION_COMPONENT_COUNT = 2;

    // 需要的纹理区域坐标
    // 纹理坐标 左下角： 0，0  左上角 0,1  右上角 1，1 右下角 1，0
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;

    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PPER_FLOAT;

    private static  final float[] VERTEX_DATA = {

            // X,Y,S,T
            0.f,0.f,0.5f,0.5f,
            -0.5f,-0.8f,0.f,0.9f,
            0.5f,-0.8f,1.f,0.9f,
            0.5f,0.8f,1.f,0.1f,
            -0.5f,0.8f,0.f,0.1f,
            -0.5f,-0.8f,0.f,0.9f,
    };

    private final  VertexArray vertexArray;

    public Table()
    {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    /**
     * 将数据绑定到 TextureShaderProgram
     * @param textureShaderProgram
     */
    public void bindData(TextureShaderProgram textureShaderProgram)
    {
        vertexArray.setVertexAttribPointer(
                0,
                textureShaderProgram.getPositionAttriLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE
                );

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureShaderProgram.getTextureCoordiatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE
        );
    }

    /**
     * 绘制桌子
     */
    public void draw()
    {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,6);
    }

}
