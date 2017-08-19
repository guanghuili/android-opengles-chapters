package com.example.ligh.camera.utils;

import android.util.Log;

/**
 * Created by gh.li on 2017/8/3.
 */

public class GLog
{
    private static final boolean ON = true;

    private static final String TAG = "GLog";

    public static void i(String msg)
    {
        if (ON)
            Log.i(TAG,msg);
    }
}
