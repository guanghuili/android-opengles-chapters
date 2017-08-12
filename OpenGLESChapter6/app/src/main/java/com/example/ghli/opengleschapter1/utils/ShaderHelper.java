package com.example.ghli.opengleschapter1.utils;

import android.opengl.GLES20;

import com.example.ghli.opengleschapter1.GLog;

/**
 * Created by gh.li on 2017/8/3.
 */

public class ShaderHelper
{
    public static int compileVertexShader(String shaderCode)
    {
        return compileShader(GLES20.GL_VERTEX_SHADER,shaderCode);
    }

    public static int compileFragmentShader(String shaderCode)
    {
        return compileShader(GLES20.GL_FRAGMENT_SHADER,shaderCode);
    }

    public static int compileShader(int type,String shaderCode)
    {
        // 创建一个着色器对象
        final int shareObjectId = GLES20.glCreateShader(type);

        if(shareObjectId == 0)
        {
            GLog.i("Could not crate new shader.");
            return 0;
        }

        // 上传源代码
        GLES20.glShaderSource(shareObjectId,shaderCode);

        // 编译上传到 shareObjectId 的源代码
        GLES20.glCompileShader(shareObjectId);

        // 获取编译状态
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shareObjectId,GLES20.GL_COMPILE_STATUS,compileStatus,0);

        GLog.i("compileShader : " + GLES20.glGetShaderInfoLog(shareObjectId));

        if (compileStatus[0] == 0)
        {
            GLES20.glDeleteShader(shareObjectId);

            GLog.i("compileShader failed");
        }

        return shareObjectId;
    }

    public static int linkProgram(int vertexShaderId,int framentShaderId)
    {
        final int programObjectId = GLES20.glCreateProgram();

        if (programObjectId == 0)
        {
            GLog.i("glCreateProgram failed");
            return 0;
        }

        GLES20.glAttachShader(programObjectId,vertexShaderId);
        GLES20.glAttachShader(programObjectId,framentShaderId);

        GLES20.glLinkProgram(programObjectId);


        // 获取编译状态
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId,GLES20.GL_LINK_STATUS,linkStatus,0);

        GLog.i("linkProgram : " + GLES20.glGetProgramInfoLog(programObjectId));

        if (linkStatus[0] == 0)
        {
            GLES20.glDeleteProgram(programObjectId);

            GLog.i("linkProgram failed");
        }else
        {
            GLog.i("linkProgram sucress");
        }


        return programObjectId;
    }


    public static int buildProgram(String vertexShaderSource,String framentShaderSource)
    {
        int vertextShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(framentShaderSource);

        int program = ShaderHelper.linkProgram(vertextShader,fragmentShader);

        return program;
    }
}
