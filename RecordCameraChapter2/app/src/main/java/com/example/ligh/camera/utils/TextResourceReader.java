package com.example.ligh.camera.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by gh.li on 2017/8/3.
 */

public class TextResourceReader
{
    public static  String readTextFileFromResource(Context context,int resourceId)
    {
        StringBuilder body = new StringBuilder();

        InputStream inputStream = context.getResources().openRawResource(resourceId);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferReader = new BufferedReader(inputStreamReader);

        String nextLine = null;

        try {
            while ( (nextLine = bufferReader.readLine()) != null)
                body.append(nextLine).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return body.toString();
    }

}
