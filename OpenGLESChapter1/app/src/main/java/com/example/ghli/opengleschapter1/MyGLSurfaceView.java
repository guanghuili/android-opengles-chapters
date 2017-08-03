package com.example.ghli.opengleschapter1;


import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by gh.li on 2017/8/3.
 */

/**
 * 在幕后GLSurfaceView 为自己创建了一个window , 他没有动画或者变形效果因为他是window的一部分
 */
public class MyGLSurfaceView extends GLSurfaceView
{
    public MyGLSurfaceView(Context context){
        super(context);
// 创建一个OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        //设置Renderer到GLSurfaceView
        setRenderer(new MyGL20Renderer());
    }
}
