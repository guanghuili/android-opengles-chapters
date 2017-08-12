package com.example.ghli.opengleschapter1.objects;

import android.opengl.GLES20;

import com.example.ghli.opengleschapter1.Constants;
import com.example.ghli.opengleschapter1.data.VertexArray;
import com.example.ghli.opengleschapter1.programs.ColorShaderProgram;

/**
 * Created by ligh on 2017/8/12.
 */

public class Mallet
{
    // 桌子在屏幕的位置坐标
    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final int COLOR_COMPONENT_COUNT = 3;

    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * Constants.BYTES_PPER_FLOAT;

    private static  final float[] VERTEX_DATA = {

            // X,Y,R,G,B
            0.f,-0.4f,0f,0f,1f,
            0.f,0.4f,1.f,0.f,0.f
    };

    private final VertexArray vertexArray;

    public Mallet()
    {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    /**
     * 将数据绑定到 ColorShaderProgram
     * @param colorShaderProgram
     */
    public void bindData(ColorShaderProgram colorShaderProgram)
    {
        vertexArray.setVertexAttribPointer(
                0,
                colorShaderProgram.getPositionAttriLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE
        );

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                colorShaderProgram.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT,
                STRIDE
        );
    }

    /**
     * 绘制木槌
     */
    public void draw()
    {
        GLES20.glDrawArrays(GLES20.GL_POINTS,0,2);
    }
}
