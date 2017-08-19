package com.example.ligh.camera.programs;

/**
 * Created by ligh on 2017/8/12.
 */

import android.content.Context;
import android.opengl.GLES20;

import com.example.ligh.camera.R;

/**
 * 颜色着色器程序
 */
public class ColorShaderProgram extends ShaderProgram
{
    private final int uMatrixLocation;
    // 位置
    private final int aPositionLocation;
    // 颜色
    private final int aColorLocation;

    public ColorShaderProgram(Context context)
    {
        super(context, R.raw.simple_vertex_shader,
                R.raw.simple_fragment_shader);

        // 读取属性位置
        uMatrixLocation = getUniiformLocation(U_MATRIX);

        aPositionLocation = getAttribLoation(A_POSITION);
        aColorLocation =  getAttribLoation(A_COLOR);

    }

    public void setUniforms(float[] matrix)
    {
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0);
    }

    public int getPositionAttriLocation()
    {
        return aPositionLocation;
    }

    public int getColorAttributeLocation()
    {
        return aColorLocation;
    }
}
