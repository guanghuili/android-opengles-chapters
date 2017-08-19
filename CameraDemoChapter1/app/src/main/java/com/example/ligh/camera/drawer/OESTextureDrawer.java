package com.example.ligh.camera.drawer;

import android.opengl.GLES20;

import com.example.ligh.camera.utils.Constants;
import com.example.ligh.camera.data.VertexArray;
import com.example.ligh.camera.programs.OESTextureShaderProgram;

/**
 * Created by ligh on 2017/8/19.
 */

public class OESTextureDrawer
{
    private static final int POSITION_COMPONENT_COUNT = 2;

    // 需要的纹理区域坐标
    // 纹理坐标 左下角： 0，0  左上角 0,1  右上角 1，1 右下角 1，0
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;

    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PPER_FLOAT;

    private static  final float[] VERTEX_DATA =
            {

            // X,Y,X,Y
            0.f,0.f,0.5f,0.5f,
            -1f,1f,0.f,1f,
            -1f,-1f,1.f,1f,
            1f,-1f,1.f,0.f,
            1f,1f,0.f,0.f,
            -1f,1f,0.f,1f,


    };


    private final VertexArray vertexArray;

    public OESTextureDrawer()
    {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    /**
     * 将数据绑定到 OESTextureShaderProgram
     * @param textureShaderProgram
     */
    public void bindData(OESTextureShaderProgram textureShaderProgram)
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
     * 绘制纹理
     */
    public void draw()
    {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,VERTEX_DATA.length);
    }

}
