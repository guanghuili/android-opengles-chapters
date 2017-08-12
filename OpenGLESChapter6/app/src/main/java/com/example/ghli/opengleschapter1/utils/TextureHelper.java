package com.example.ghli.opengleschapter1.utils;

/**
 * Created by ligh on 2017/8/12.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.ghli.opengleschapter1.GLog;

/**
 * 纹理助手
 */
public class TextureHelper
{
    private  static  final String TAG = "TextureHelper";

    public static int loadTexture(Context context,int resourceId)
    {
        // 存放生成的纹理id
        final int[] texttureObjectIds = new int[1];

        // 生成一个纹理
        GLES20.glGenTextures(1,texttureObjectIds,0);


        if (texttureObjectIds[0] == 0)
        {
            GLog.i("loadTexture ： generate a new OpenGL texture object failed");

            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resourceId,options);

        if (bitmap == null)
        {
            GLog.i("loadTexture ： resourceId  could not be decoded");

            GLES20.glDeleteBuffers(1,texttureObjectIds,0);

            return 0;
        }

        // 绑定纹理 告诉 OpenGL 后边的纹理调用都应用这个纹理对象
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texttureObjectIds[0]);

        // 设置纹理过滤模式
        // 设置纹理缩小时过滤模式 ：三线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR);
        // 设置纹理放大是过滤模式：双线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR);

        // 加载纹理到 OpenGL 并返回 ID
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);

        // 图像被加载到 OpenGL 后我们就不需要在持有 Android 的位图了
        bitmap.recycle();

        // 生成所有必要级别的 MIP 贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        // 完成纹理加载后 解除纹理和当前的绑定 这样我们就不会把其他纹理的方法应用到这个纹理上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        GLog.i("loadTexture success id :  "+texttureObjectIds[0]);

        return texttureObjectIds[0];
}

}

