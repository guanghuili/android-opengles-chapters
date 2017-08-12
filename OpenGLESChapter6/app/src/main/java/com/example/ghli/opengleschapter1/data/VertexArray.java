package com.example.ghli.opengleschapter1.data;

/**
 * Created by ligh on 2017/8/12.
 */

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.example.ghli.opengleschapter1.Constants.BYTES_PPER_FLOAT;

/**
 * 用来封装存储顶点矩阵的FloatBuffer
 */
public class VertexArray
{

    // 实际的顶点数据
    private final FloatBuffer floatBuffer;

    public VertexArray(float[] vertexData)
    {
        floatBuffer = ByteBuffer.allocateDirect(vertexData.length * BYTES_PPER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }


    public void setVertexAttribPointer(int dataOffset,int attributeLocation,int componentCount,int stride)
    {
        // 偏移量
        floatBuffer.position(dataOffset);

        GLES20.glVertexAttribPointer(attributeLocation,componentCount,GLES20.GL_FLOAT,false,stride,floatBuffer);

        GLES20.glEnableVertexAttribArray(attributeLocation);

        floatBuffer.position(0);
    }
}
