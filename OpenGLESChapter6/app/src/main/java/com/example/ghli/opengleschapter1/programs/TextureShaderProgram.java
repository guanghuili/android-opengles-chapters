package com.example.ghli.opengleschapter1.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.example.ghli.opengleschapter1.R;

/**
 * Created by ligh on 2017/8/12.
 */

/**
 * 纹理绘制程序
 */
public class TextureShaderProgram extends ShaderProgram
{

    private final int uMatrixLocation;

    // 纹理数据位置
    private final int uTextureUnitLocation;

    // 位置
    private final int aPositionLocation;
    // 纹理坐标
    private final int aTextureCoordiatesLocation;

    public TextureShaderProgram(Context context)
    {
        super(context, R.raw.texture_vertex_shader,
                R.raw.texture_fragment_shader);

        // 读取属性位置
        uMatrixLocation = getUniiformLocation(U_MATRIX);
        uTextureUnitLocation = getUniiformLocation(U_TEXTURE_UNIT);

        aPositionLocation = getAttribLoation(A_POSITION);
        aTextureCoordiatesLocation = getAttribLoation(A_TEXTURE_COORDINATES);
    }

    public void setUniforms(float[] matrix,int textureId)
    {
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0);

        // 设置激活的 texture unit 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);

        // 上传 texture unit 0 位置的纹理到 OpenGL
        GLES20.glUniform1i(uTextureUnitLocation,0);
    }

    public int getPositionAttriLocation()
    {
        return aPositionLocation;
    }

    public int getTextureCoordiatesAttributeLocation()
    {
        return aTextureCoordiatesLocation;
    }
}
