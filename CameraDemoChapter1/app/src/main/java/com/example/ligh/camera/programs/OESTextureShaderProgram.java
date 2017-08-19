package com.example.ligh.camera.programs;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.example.ligh.camera.R;

/**
 * Created by ligh on 2017/8/19.
 */

public class OESTextureShaderProgram extends  ShaderProgram
{
    private final int uMatrixLocation;

    // 纹理数据位置
    private final int uTextureUnitLocation;

    // 位置
    private final int aPositionLocation;
    // 纹理坐标
    private final int aTextureCoordiatesLocation;

    public OESTextureShaderProgram(Context context)
    {
        super(context, R.raw.oes_texture_vertex_shader,
                R.raw.oes_texture_fragment_shader);

        // 读取属性位置
        uMatrixLocation = getUniiformLocation(U_MATRIX);
        uTextureUnitLocation = getUniiformLocation(U_TEXTURE_UNIT);

        aPositionLocation = getAttribLoation(A_POSITION);
        aTextureCoordiatesLocation = getAttribLoation(A_TEXTURE_COORDINATES);
    }

    public void setUniforms(float[] matrix,int textureId)
    {
//        GLog.i("setUniforms textureId = "+textureId);

        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0);

        // 设置激活的 texture unit 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);

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
