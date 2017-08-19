package com.example.ligh.camera.programs;

/**
 * Created by ligh on 2017/8/12.
 */

import android.content.Context;
import android.opengl.GLES20;

import com.example.ligh.camera.utils.ShaderHelper;
import com.example.ligh.camera.utils.TextResourceReader;

/**
 *
 */
public class ShaderProgram
{
    // 定义属性名称
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";

    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    protected final int program;

    protected ShaderProgram(Context context,int vertexShaderResourceId,int fragmentShaderResourceId)
    {
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(context,vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(context,fragmentShaderResourceId)
                );
    }

    public void useProgram()
    {
        GLES20.glUseProgram(program);
    }

    protected int getUniiformLocation(String unformName)
    {
        return GLES20.glGetUniformLocation(program,unformName);
    }

    protected int getAttribLoation(String attributeName)
    {
        return GLES20.glGetAttribLocation(program,attributeName);
    }
}
